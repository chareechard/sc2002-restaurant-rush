from pathlib import Path
import xml.etree.ElementTree as E
from html import escape
import math,json,sys
from validate_layout import validate
from PIL import ImageFont
WORK=Path(__file__).resolve().parent
OUT=WORK.parent
INK='#20364F'
FONT=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',16)
BOLD=ImageFont.truetype('C:/Windows/Fonts/arialbd.ttf',17)
SMALL=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',14)
def rect_intersects(a,b):
    return a[0]<b[0]+b[2] and b[0]<a[0]+a[2] and a[1]<b[1]+b[3] and b[1]<a[1]+a[3]


def line_hits_box(a,b,box,pad=4):
    x,y,w,h=box;x-=pad;y-=pad;w+=2*pad;h+=2*pad
    if a[0]==b[0]:return x<=a[0]<=x+w and max(min(a[1],b[1]),y)<=min(max(a[1],b[1]),y+h)
    if a[1]==b[1]:return y<=a[1]<=y+h and max(min(a[0],b[0]),x)<=min(max(a[0],b[0]),x+w)
    return False

def member_lines(c, names=True):
    rows = []
    for a in c.findall('attribute'):
        sign = {'public': '+', 'private': '-', 'protected': '#', 'package': '~'}[a.get('visibility')]
        rows.append((f"{sign}{a.get('name')} : {a.get('type')}", a.get('static') == 'true', False, 'field'))
    for literal in c.findall('literal'):
        rows.append((literal.get('name'), False, False, 'literal'))
    for o in c.findall('operation'):
        sign = {'public': '+', 'private': '-', 'protected': '#', 'package': '~'}[o.get('visibility')]
        params = ', '.join((p.get('name')+' : ' if names else '')+p.get('type') for p in o.findall('parameter'))
        suffix = '' if o.get('constructor') == 'true' else ' : '+o.get('returns')
        rows.append((f"{sign}{o.get('name')}({params}){suffix}", o.get('static') == 'true', o.get('abstract') == 'true', 'operation'))
    return rows

class Svg:
    def __init__(self,w,h):
        self.parts=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}">',
                    '<rect width="100%" height="100%" fill="white"/>']
    def rect(self,x,y,w,h,fill='white',stroke='none',weight=1):
        self.parts.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{fill}" stroke="{stroke}" stroke-width="{weight}"/>')
    def line(self,x1,y1,x2,y2,color=INK,weight=1.4,dashed=False):
        dash=' stroke-dasharray="8 6"' if dashed else ''
        self.parts.append(f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{color}" stroke-width="{weight}"{dash}/>')
    def text(self,x,y,text,size=16,bold=False,italic=False,anchor='start',color=INK):
        self.parts.append(f'<text x="{x}" y="{y}" fill="{color}" font-family="Arial" font-size="{size}" font-weight="{"bold" if bold else "normal"}" font-style="{"italic" if italic else "normal"}" text-anchor="{anchor}">{escape(text)}</text>')
    def polygon(self,points,fill='white',stroke=INK):
        self.parts.append('<polygon points="'+' '.join(f'{x},{y}' for x,y in points)+f'" fill="{fill}" stroke="{stroke}" stroke-width="1.6"/>')
    def end_symbol(self,p,adjacent,kind):
        dx,dy=adjacent[0]-p[0],adjacent[1]-p[1];length=math.hypot(dx,dy);ux,uy=dx/length,dy/length;vx,vy=-uy,ux
        def pt(u,v):return (p[0]+ux*u+vx*v,p[1]+uy*u+vy*v)
        if kind in ['composition','aggregation']:
            self.polygon([pt(0,0),pt(13,7),pt(26,0),pt(13,-7)],INK if kind=='composition' else 'white')
        elif kind in ['generalization','realization']:
            self.polygon([pt(0,0),pt(18,9),pt(18,-9)])
        elif kind=='dependency':
            a,b,c=pt(14,6),pt(0,0),pt(14,-6)
            self.line(*a,*b);self.line(*b,*c)
    def save(self,path):path.write_text('\n'.join(self.parts+['</svg>']),encoding='utf-8')


def draw(stage,classes,nodes,headings,edges,width,height):
    legend_y=height+70
    svg=Svg(width+40,legend_y+220)
    svg.text(60,48,f'Restaurant Rush - Stage {stage} Class Diagram',30,True)
    for text,x,y,w in headings:
        svg.rect(x,y,w,30,'#F1F5F9')
        svg.text(x+7,y+22,text,19,True)
    for e in edges:
        points=e['points'];d='M '+' L '.join(f'{x} {y}' for x,y in points)
        dash=' stroke-dasharray="8 6"' if e['kind'] in ['dependency','realization'] else ''
        svg.parts.append(f'<path d="{d}" fill="none" stroke="#52677D" stroke-width="1.5"{dash}/>')
        if e['kind'] in ['composition','aggregation']:svg.end_symbol(points[0],points[1],e['kind'])
        if e['kind'] in ['dependency','generalization','realization']:svg.end_symbol(points[-1],points[-2],e['kind'])
    # Short number labels sit beside the end segment on an opaque white background.
    label_boxes=[]
    for e in edges:
        for end in ['source','target']:
            value=e.get(end+'Multiplicity','')
            if value=='Unspecified':value=''
            if not value:continue
            p=e[end+'_port'];side=e[end+'_side'];tw=math.ceil(SMALL.getlength(value))+8;th=22
            box=e[end+'_label']
            collision=sum(rect_intersects(box,(n['x']-4,n['y']-4,n['w']+8,n['h']+8)) for n in nodes.values())
            collision+=sum(rect_intersects(box,b) for b in label_boxes)
            collision+=sum(rect_intersects(box,(hx,hy,hw,30)) for _,hx,hy,hw in headings)
            collision+=sum(line_hits_box(a,b,box) for path in edges for a,b in zip(path['points'],path['points'][1:]))
            assert collision==0,('Multiplicity overlaps a connector or shape',e['source'],e['target'],value)
            label_boxes.append(box);e[end+'_label']=list(box);x,y,w,h=box
            svg.rect(x,y,w,h)
            svg.text(x+4,y+16,value,14)
    for name,n in nodes.items():
        c=n['class'];x,y,w,h=n['x'],n['y'],n['w'],n['h'];header=n['header']
        stroke='#D16D00' if c.get('modified')=='true' else '#52677D'
        svg.rect(x,y,w,h,c.get('color'),stroke,2.6 if c.get('modified')=='true' else 1.3)
        if header>30:
            kind='interface' if c.get('kind')=='interface' else 'enumeration' if c.get('kind')=='enum' else 'exception'
            svg.text(x+w/2,y+17,'«'+kind+'»',13,anchor='middle')
        svg.text(x+w/2,y+header-10,name,17,True,c.get('kind')=='abstract class','middle')
        svg.line(x,y+header,x+w,y+header,stroke,1)
        baseline=y+header+22;last=None
        for text,static,abstract,group in n['rows']:
            if group=='operation' and last=='field':
                svg.line(x,baseline-17,x+w,baseline-17,stroke,1);baseline+=6
            svg.text(x+10,baseline,text,16,italic=abstract)
            if static:svg.line(x+10,baseline+2,x+10+FONT.getlength(text),baseline+2,INK,0.9)
            baseline+=22;last=group
    kinds=['association','composition','aggregation','dependency','generalization','realization']
    if stage==3:kinds=['association','composition','aggregation','dependency','generalization','realization']
    labels={'association':'Association (reference)','composition':'Composition','aggregation':'Shared aggregation',
            'dependency':'Dependency (uses)','generalization':'Inheritance','realization':'Interface implementation'}
    columns=3
    step=(width-120)/columns
    for index,kind in enumerate(kinds):
        if stage<3 and kind in ['aggregation','realization']:continue
        x=60+(index%columns)*step;y=legend_y+(index//columns)*52
        svg.line(x,y,x+90,y,dashed=kind in ['dependency','realization'])
        if kind in ['composition','aggregation']:svg.end_symbol((x,y),(x+90,y),kind)
        if kind in ['dependency','generalization','realization']:svg.end_symbol((x+90,y),(x,y),kind)
        svg.text(x+110,y+6,labels[kind],18)
    rows=math.ceil(len(kinds)/columns)
    note_y=legend_y+rows*52+12
    svg.text(60,note_y,'Underlined = static (class member).',17)
    svg.line(60,note_y+2,60+FONT.getlength('Underlined'),note_y+2)
    svg.text(435,note_y,'Italic = abstract.',17,italic=True)
    color_y=note_y+40
    samples=[('Stage 1: white', '#FFFFFF'),('Stage 2 additions: blue','#D8EAFE'),('Stage 3 additions: green','#DDF1DF')]
    for i,(label,color) in enumerate(samples):
        x=60+i*(width-120)/4
        svg.rect(x,color_y,(width-180)/4,32,color)
        svg.text(x+8,color_y+23,label,17)
    x=60+3*(width-120)/4
    svg.rect(x,color_y,32,30,'white','#D16D00',2.6)
    svg.text(x+45,color_y+23,'Modified existing class',17)
    path=OUT/f'Stage_{stage}_Class_Diagram.svg';svg.save(path)
    print('Wrote',path,flush=True)

if __name__=='__main__':
    project=E.parse(WORK/'model.xml').getroot().find('project')
    stage=int(project.get('stage'))
    layout=json.loads((WORK/'layout.json').read_text(encoding='utf-8'))
    audit=validate(layout)
    assert not audit['issues'], audit
    classes={c.get('name'):c for c in project.find('classes')}
    nodes=layout['nodes']
    for name,n in nodes.items():
        n['class']=classes[name]
        n['rows']=member_lines(classes[name],n['show_names'])
    draw(stage,classes,nodes,layout['headings'],layout['edges'],layout['width'],layout['height']-290)
