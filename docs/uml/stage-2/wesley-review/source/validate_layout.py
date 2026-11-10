"""Check the visible geometry of the class diagram without changing its model."""
from pathlib import Path
import json,math

def point_distance(p,a,b):
    dx=b[0]-a[0];dy=b[1]-a[1]
    t=max(0,min(1,((p[0]-a[0])*dx+(p[1]-a[1])*dy)/(dx*dx+dy*dy)))
    return math.hypot(p[0]-a[0]-t*dx,p[1]-a[1]-t*dy)

def cross(a,b,c):return (b[0]-a[0])*(c[1]-a[1])-(b[1]-a[1])*(c[0]-a[0])

def segment_distance(a,b,c,d):
    bounded=(max(min(a[0],b[0]),min(c[0],d[0]))<=min(max(a[0],b[0]),max(c[0],d[0])) and
             max(min(a[1],b[1]),min(c[1],d[1]))<=min(max(a[1],b[1]),max(c[1],d[1])))
    if bounded and cross(a,b,c)*cross(a,b,d)<=0 and cross(c,d,a)*cross(c,d,b)<=0:return 0
    return min(point_distance(a,c,d),point_distance(b,c,d),point_distance(c,a,b),point_distance(d,a,b))

def hit_box(a,b,box,pad=0):
    x,y,w,h=box;x-=pad;y-=pad;w+=pad*2;h+=pad*2
    if a[0]==b[0]:return x<a[0]<x+w and max(min(a[1],b[1]),y)<min(max(a[1],b[1]),y+h)
    if a[1]==b[1]:return y<a[1]<y+h and max(min(a[0],b[0]),x)<min(max(a[0],b[0]),x+w)
    raise AssertionError('Nonorthogonal connector')

def head_polygon(edge):
    kind=edge['kind'];points=edge['points']
    if kind in ['composition','aggregation']:p,adj=points[:2];shape=[(0,0),(13,7),(26,0),(13,-7)]
    elif kind in ['generalization','realization']:p,adj=points[-1],points[-2];shape=[(0,0),(18,9),(18,-9)]
    elif kind=='dependency':p,adj=points[-1],points[-2];shape=[(0,0),(14,6),(14,-6)]
    else:return None
    dx=adj[0]-p[0];dy=adj[1]-p[1];size=math.hypot(dx,dy);ux=dx/size;uy=dy/size
    return [(p[0]+ux*u-uy*v,p[1]+uy*u+ux*v) for u,v in shape]

def validate(layout):
    edges=layout['edges'];segments=[(i,a,b) for i,e in enumerate(edges) for a,b in zip(e['points'],e['points'][1:])]
    issues=[];parallel_min=float('inf');head_min=float('inf');labels=0
    for n,(i,a,b) in enumerate(segments):
        for k,c,d in segments[n+1:]:
            if i==k:continue
            if a[0]==b[0] and c[0]==d[0]:
                overlap=min(max(a[1],b[1]),max(c[1],d[1]))-max(min(a[1],b[1]),min(c[1],d[1]));gap=abs(a[0]-c[0])
            elif a[1]==b[1] and c[1]==d[1]:
                overlap=min(max(a[0],b[0]),max(c[0],d[0]))-max(min(a[0],b[0]),min(c[0],d[0]));gap=abs(a[1]-c[1])
            else:continue
            if overlap>0:
                parallel_min=min(parallel_min,gap)
                if gap<28:issues.append({'kind':'parallel','edges':[i,k],'gap':gap})
    for i,e in enumerate(edges):
        polygon=head_polygon(e)
        if polygon:
            sides=list(zip(polygon,polygon[1:]+polygon[:1]))
            for k,a,b in segments:
                if i==k:continue
                distance=min(segment_distance(a,b,c,d) for c,d in sides)
                head_min=min(head_min,distance)
                if distance<24-1e-6:issues.append({'kind':'arrowhead','head':i,'line':k,'gap':round(distance,2)})
        for end in ['source','target']:
            box=e.get(end+'_label')
            if not box:continue
            labels+=1
            for k,a,b in segments:
                if hit_box(a,b,box,6):issues.append({'kind':'label','label':[i,end],'line':k})
    result={'classes':len(layout['nodes']),'relationships':len(edges),'labels_checked':labels,
            'minimum_parallel_gap':parallel_min,'minimum_arrowhead_gap':round(head_min,2),'issues':issues}
    return result

if __name__=='__main__':
    result=validate(json.loads((Path(__file__).with_name('layout.json')).read_text(encoding='utf-8')))
    print(json.dumps(result,indent=2))
    if result['issues']:raise SystemExit(1)
