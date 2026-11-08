"""Check UML provenance, native storage, exported members, layouts and checksums.

Requires Python 3.11+, Git and JDK 17+. Uses only the Python standard library.
Earlier-stage sources are read from the pinned commits in this repository.
This checker does not open Visual Paradigm or certify a GUI review.
"""

from pathlib import Path
import argparse
import hashlib
import io
import json
import os
import re
import runpy
import sqlite3
import subprocess
import tarfile
import tempfile
import xml.etree.ElementTree as ET


ROOT = Path(__file__).resolve().parents[1]


def command(args, cwd=ROOT):
    result = subprocess.run(args, cwd=cwd, capture_output=True, text=True)
    if result.returncode:
        raise RuntimeError(" ".join(map(str, args)) + "\n" + result.stdout + result.stderr)
    return result.stdout.strip()


def tree_digest(files):
    digest = hashlib.sha256()
    for path, content in sorted(files.items()):
        digest.update(path.encode("utf-8") + b"\0" + content + b"\0")
    return digest.hexdigest()


def pinned_sources(commit):
    command(["git", "cat-file", "-e", commit + "^{commit}"])
    command(["git", "merge-base", "--is-ancestor", commit, "HEAD"])
    data = subprocess.check_output(["git", "archive", commit, "src/main/java"], cwd=ROOT)
    with tarfile.open(fileobj=io.BytesIO(data)) as archive:
        return {member.name: archive.extractfile(member).read()
                for member in archive.getmembers() if member.isfile()}


def native_descriptor(path, source_files):
    """Convert actual model definitions, excluding diagram reference elements."""
    root = ET.parse(path).getroot()
    packages = {Path(p).stem: ".".join(Path(p).parts[3:-1]) for p in source_files}
    output = ET.Element("classes")
    models = root.find("Models")
    if models is None:
        raise ValueError("Missing native Models element: " + str(path))
    for model in models.iter("Class"):
        if "Id" not in model.attrib:
            continue
        name = model.get("Name")
        if name not in packages:
            raise ValueError("Model type is absent from pinned source: " + str(name))
        stereotypes = {s.get("Name", "").lower() for s in model.findall("Stereotypes/Stereotype")}
        kind = "enum" if "enumeration" in stereotypes else "interface" if "interface" in stereotypes else (
            "abstract class" if model.get("Abstract") == "true" else "class")
        cls = ET.SubElement(output, "class", name=name, package=packages[name], kind=kind)
        children = model.find("ModelChildren")
        if children is None:
            continue
        for member in children:
            if member.tag == "Attribute":
                ET.SubElement(cls, "attribute", name=member.get("Name"), type=member.get("Type", ""),
                              visibility=member.get("Visibility", "package"),
                              readonly=member.get("ReadOnly", "false"),
                              static=str(member.get("Scope") == "classifier").lower())
            elif member.tag == "Operation":
                constructor = member.get("Name") == name
                operation = ET.SubElement(cls, "operation", name=member.get("Name"),
                    returns=member.get("ReturnType", "void"), constructor=str(constructor).lower(),
                    visibility=member.get("Visibility", "package"),
                    static=member.get("Static", "false"), abstract=member.get("Abstract", "false"))
                for param in member.findall("ModelChildren/Parameter"):
                    if param.get("Direction") != "return":
                        ET.SubElement(operation, "parameter", name=param.get("Name", ""), type=param.get("Type", ""))
            elif member.tag == "EnumerationLiteral":
                ET.SubElement(cls, "literal", name=member.get("Name"))
    if not list(output):
        raise ValueError("No model classes found: " + str(path))
    return output


def check_native(path):
    # Deserialize into memory so the native file is never opened for writing.
    connection = sqlite3.connect(":memory:")
    connection.deserialize(path.read_bytes())
    try:
        result = connection.execute("PRAGMA quick_check").fetchall()
        if result != [("ok",)]:
            raise ValueError(str(path) + ": " + str(result))
        return {table: connection.execute('SELECT count(*) FROM "' + table + '"').fetchone()[0]
                for table in ("MODEL_ELEMENT", "DIAGRAM", "DIAGRAM_ELEMENT")}
    finally:
        connection.close()


def check_manifest(path, scratch, cache):
    metadata = json.loads(path.read_text(encoding="utf-8"))
    commit = metadata["source_commit"]
    if not re.fullmatch(r"[0-9a-f]{40}", commit):
        raise ValueError("Expected a full source commit: " + str(path))
    if commit not in cache:
        sources = pinned_sources(commit)
        folder = scratch / commit
        classes = folder / "classes"
        classes.mkdir(parents=True)
        source_paths = []
        for relative, data in sources.items():
            target = folder / relative
            if ".." in Path(relative).parts or not relative.startswith("src/main/java/"):
                raise ValueError("Unexpected archive path: " + relative)
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
            source_paths.append(str(target))
        command(["javac", "--release", "17", "-encoding", "UTF-8", "-Xlint:all", "-Werror", "-d", str(classes),
                 *source_paths, str(ROOT / "tools/VerifyUmlMembers.java")])
        cache[commit] = sources, classes
    sources, classes = cache[commit]
    if tree_digest(sources) != metadata["source_files_sha256"]:
        raise ValueError("Pinned source digest mismatch: " + str(path))
    for relative, expected in metadata["artifact_sha256"].items():
        artifact = path.parent / relative
        data = artifact.read_bytes()
        if metadata.get("text_hashes") == "LF normalized" and artifact.suffix in (".md", ".xml", ".svg", ".java", ".py", ".json"):
            data = data.replace(b"\r\n", b"\n")
        if hashlib.sha256(data).hexdigest() != expected:
            raise ValueError("Artifact checksum mismatch: " + str(artifact))
    native = {p.name: check_native(p) for p in sorted(path.parent.glob("*.vpp"))}
    selected = path.parent / metadata["member_descriptor"]
    descriptor = native_descriptor(selected, sources) if selected.name == "project.xml" else ET.parse(selected).getroot()
    exported = scratch / (hashlib.sha256(str(path).encode()).hexdigest() + ".xml")
    ET.ElementTree(descriptor).write(exported, encoding="utf-8", xml_declaration=True)
    members = json.loads(command(["java", "-cp", str(classes), "VerifyUmlMembers", str(exported)]))
    native_xml = path.parent / "model-xml/project.xml"
    if selected != native_xml:
        ET.ElementTree(native_descriptor(native_xml, sources)).write(exported, encoding="utf-8", xml_declaration=True)
        native_members = json.loads(command(["java", "-cp", str(classes), "VerifyUmlMembers", str(exported)]))
    else:
        native_members = members
    geometry = None
    validator = path.parent / "source/validate_layout.py"
    if validator.exists():
        layout = json.loads((validator.parent / "layout.json").read_text(encoding="utf-8"))
        geometry = runpy.run_path(str(validator))["validate"](layout)
        if geometry["issues"]:
            raise ValueError("Geometry issues: " + str(path))
    return {"manifest": path.relative_to(ROOT).as_posix(), "source_commit": commit,
            "source_files_sha256": tree_digest(sources), "selected_members": members,
            "native_export_members": native_members, "native_storage": native, "geometry": geometry}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--json", action="store_true", help="Print machine-readable verification results")
    args = parser.parse_args()
    with tempfile.TemporaryDirectory(prefix="restaurant-rush-uml-") as temporary:
        scratch = Path(temporary)
        cache = {}
        results = [check_manifest(p, scratch, cache) for p in sorted((ROOT / "docs/uml").rglob("verification.json"))]
    if args.json:
        print(json.dumps(results, indent=2))
    else:
        for result in results:
            members = result["selected_members"]
            print("PASS:", result["manifest"], "-", members["types"], "types; native storage, members and hashes checked")
        print("Visual Paradigm GUI reopen remains a separate manual check.")


if __name__ == "__main__":
    main()
