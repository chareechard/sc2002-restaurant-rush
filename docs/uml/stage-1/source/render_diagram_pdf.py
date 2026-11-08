"""Render the native Visual Paradigm SVG as a single vector PDF page.

Optional authoring dependencies: reportlab and svglib. This is outside the game.
Pass the diagram folder as the first argument, or use the enclosing stage folder.
"""

from pathlib import Path
import os
import sys

from reportlab.graphics import renderPDF
from reportlab.lib.pagesizes import A2, landscape
from reportlab.pdfgen import canvas
from svglib.fonts import FontMap
from svglib.svglib import svg2rlg


def render(folder):
    fonts = FontMap()
    font_folder = Path(os.environ.get('WINDIR', 'C:/Windows')) / 'Fonts'
    variants = [
        ('normal', 'normal', 'arial.ttf', 'Helvetica'),
        ('bold', 'normal', 'arialbd.ttf', 'Helvetica-Bold'),
        ('normal', 'italic', 'ariali.ttf', 'Helvetica-Oblique'),
        ('bold', 'italic', 'arialbi.ttf', 'Helvetica-BoldOblique'),
    ]
    for family in ['Arial', 'Dialog', 'SansSerif', 'Arial Unicode MS']:
        for weight, style, filename, fallback in variants:
            path = font_folder / filename
            if path.exists():
                fonts.register_font(family, str(path), weight=weight, style=style)
            else:
                fonts.register_font(family, weight=weight, style=style, rlgFontName=fallback)

    basename = 'Restaurant_Rush_Stage1_Wesley_Draft'
    drawing = svg2rlg(folder / (basename + '.svg'), font_map=fonts)
    if drawing is None:
        raise ValueError('The native SVG could not be read.')
    page_width, page_height = landscape(A2)
    scale = min((page_width - 48) / drawing.width, (page_height - 48) / drawing.height)
    pdf = canvas.Canvas(str(folder / (basename + '.pdf')), pagesize=(page_width, page_height), pageCompression=1)
    pdf.setTitle('Restaurant Rush - Stage 1 Class Diagram')
    pdf.setAuthor('SC2002 Restaurant Rush team')
    pdf.setSubject('Stage 1 UML; selected members matching Java checkpoint 9917001')
    pdf.saveState()
    pdf.translate((page_width - drawing.width * scale) / 2, (page_height - drawing.height * scale) / 2)
    pdf.scale(scale, scale)
    renderPDF.draw(drawing, pdf, 0, 0)
    pdf.restoreState()
    pdf.showPage()
    pdf.save()
    print('Rendered one A2 landscape vector PDF page.')


if __name__ == '__main__':
    render(Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[1])
