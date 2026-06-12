import { useState, useEffect } from 'react';
import { Library, Search, ExternalLink, BookOpen, Video, FileText, Link2, Star } from 'lucide-react';
import { apiClient } from '../services/api';
import type { Resource } from '../types';
import { RESOURCE_CATEGORIES } from '../constants';

const TYPE_ICONS: Record<string, React.ElementType> = { ARTICLE:BookOpen, VIDEO:Video, PDF:FileText, LINK:Link2, TOOL:Star };

const TYPE_COLORS: Record<string, { light: string; dark: string }> = {
  ARTICLE: { light:'bg-blue-50 text-blue-700',   dark:'dark:bg-blue-900/30 dark:text-blue-300' },
  VIDEO:   { light:'bg-red-50 text-red-700',     dark:'dark:bg-red-900/30 dark:text-red-300' },
  PDF:     { light:'bg-orange-50 text-orange-700',dark:'dark:bg-orange-900/30 dark:text-orange-300' },
  LINK:    { light:'bg-purple-50 text-purple-700',dark:'dark:bg-purple-900/30 dark:text-purple-300' },
  TOOL:    { light:'bg-teal-50 text-teal-700',   dark:'dark:bg-teal-900/30 dark:text-teal-300' },
};

const MOCK_RESOURCES: Resource[] = [
  { id:'1', title:'Managing Academic Stress', description:'Practical strategies to handle exam pressure and academic workload effectively.', category:'stress-management', resourceType:'ARTICLE', contentUrl:'#', viewCount:1240, isFeatured:true, createdAt:new Date().toISOString(), updatedAt:new Date().toISOString() },
  { id:'2', title:'5-Minute Mindfulness Meditation', description:'A guided meditation to help you reset and refocus during a busy day.', category:'mindfulness', resourceType:'VIDEO', contentUrl:'#', viewCount:890, isFeatured:true, createdAt:new Date().toISOString(), updatedAt:new Date().toISOString() },
  { id:'3', title:'Sleep Hygiene Guide', description:'Evidence-based tips to improve your sleep quality and wake up refreshed.', category:'sleep-hygiene', resourceType:'PDF', contentUrl:'#', viewCount:654, isFeatured:false, createdAt:new Date().toISOString(), updatedAt:new Date().toISOString() },
  { id:'4', title:'Building Healthy Relationships', description:'How to set boundaries and communicate effectively in personal relationships.', category:'relationships', resourceType:'ARTICLE', contentUrl:'#', viewCount:432, isFeatured:false, createdAt:new Date().toISOString(), updatedAt:new Date().toISOString() },
  { id:'5', title:'Nutrition for Mental Health', description:'How your diet affects your mood, energy, and cognitive performance.', category:'nutrition', resourceType:'ARTICLE', contentUrl:'#', viewCount:321, isFeatured:false, createdAt:new Date().toISOString(), updatedAt:new Date().toISOString() },
  { id:'6', title:'Exercise & Mental Wellness', description:'The science behind how physical activity boosts mental health.', category:'exercise', resourceType:'VIDEO', contentUrl:'#', viewCount:567, isFeatured:true, createdAt:new Date().toISOString(), updatedAt:new Date().toISOString() },
];

const ResourcesPage = () => {
  const [resources, setResources] = useState<Resource[]>(MOCK_RESOURCES);
  const [search, setSearch] = useState('');
  const [activeCategory, setActiveCategory] = useState('all');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    apiClient.get<{ content?: unknown[]; data?: unknown[] }>('/resources')
      .then(res => {
        const raw = res.content ?? res.data ?? [];
        if (!Array.isArray(raw) || raw.length === 0) return;
        // Normalise API response — backend returns { url, type, featured }
        // but our Resource type uses { contentUrl, resourceType, isFeatured }
        const normalised: Resource[] = (raw as Record<string, unknown>[]).map(r => ({
          id: String(r.id ?? ''),
          title: String(r.title ?? ''),
          description: String(r.description ?? ''),
          category: String(r.category ?? ''),
          resourceType: String(r.type ?? r.resourceType ?? 'ARTICLE') as Resource['resourceType'],
          contentUrl: String(r.url ?? r.contentUrl ?? '#'),
          viewCount: Number(r.viewCount ?? 0),
          isFeatured: Boolean(r.featured ?? r.isFeatured ?? false),
          createdAt: String(r.createdAt ?? new Date().toISOString()),
          updatedAt: String(r.updatedAt ?? new Date().toISOString()),
        }));
        setResources(normalised);
      })
      .catch(() => {}).finally(() => setLoading(false));
  }, []);

  const filtered = resources.filter(r =>
    (!search || r.title.toLowerCase().includes(search.toLowerCase()) || r.description.toLowerCase().includes(search.toLowerCase())) &&
    (activeCategory === 'all' || r.category === activeCategory)
  );

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-3">
          <Library size={28} className="text-sky-500"/>Resource Hub
        </h1>
        <p className="text-slate-500 dark:text-slate-400 mt-1">Curated articles, videos, and tools for your wellness journey</p>
      </div>

      {/* Search */}
      <div className="relative">
        <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"/>
        <input type="text" value={search} onChange={e => setSearch(e.target.value)} placeholder="Search resources…"
          className="w-full bg-white dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 rounded-xl pl-11 pr-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-teal-500 transition-all shadow-sm"/>
      </div>

      {/* Filters */}
      <div className="flex gap-2 flex-wrap">
        {[{ id:'all', label:'All' }, ...RESOURCE_CATEGORIES].map(({ id, label }) => (
          <button key={id} onClick={() => setActiveCategory(id)}
            className={`px-4 py-2 rounded-full text-sm font-semibold transition-all ${
              activeCategory === id
                ? 'bg-teal-500 dark:bg-teal-600 text-white shadow-sm'
                : 'bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:border-teal-400 dark:hover:border-teal-500'
            }`}>{label}</button>
        ))}
      </div>

      {loading ? (
        <div className="flex items-center justify-center py-16">
          <div className="w-8 h-8 rounded-full border-4 border-teal-500 border-t-transparent animate-spin"/>
        </div>
      ) : (
        <>
          {filtered.filter(r => r.isFeatured).length > 0 && (
            <section>
              <h2 className="text-lg font-bold text-slate-700 dark:text-slate-200 mb-4 flex items-center gap-2">
                <Star size={18} className="text-amber-400 fill-amber-400"/>Featured
              </h2>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {filtered.filter(r => r.isFeatured).map(r => <ResourceCard key={r.id} resource={r}/>)}
              </div>
            </section>
          )}
          {filtered.filter(r => !r.isFeatured).length > 0 && (
            <section>
              <h2 className="text-lg font-bold text-slate-700 dark:text-slate-200 mb-4">All Resources</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {filtered.filter(r => !r.isFeatured).map(r => <ResourceCard key={r.id} resource={r}/>)}
              </div>
            </section>
          )}
          {filtered.length === 0 && (
            <div className="text-center py-16">
              <Library size={48} className="text-slate-300 dark:text-slate-600 mx-auto mb-4"/>
              <p className="text-slate-500 dark:text-slate-400">No resources found. Try a different search or category.</p>
            </div>
          )}
        </>
      )}
    </div>
  );
};

const ResourceCard = ({ resource }: { resource: Resource }) => {
  const Icon = TYPE_ICONS[resource.resourceType] ?? BookOpen;
  const colors = TYPE_COLORS[resource.resourceType] ?? { light:'bg-slate-100 text-slate-600', dark:'dark:bg-slate-700 dark:text-slate-300' };
  return (
    <a href={resource.contentUrl} target="_blank" rel="noopener noreferrer"
      className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50 hover:shadow-lg hover:shadow-black/5 dark:hover:shadow-black/20 hover:border-teal-300/50 dark:hover:border-teal-600/50 transition-all group flex flex-col">
      <div className="flex items-start justify-between mb-3">
        <span className={`flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${colors.light} ${colors.dark}`}>
          <Icon size={12}/>{resource.resourceType}
        </span>
        <ExternalLink size={14} className="text-slate-300 dark:text-slate-600 group-hover:text-teal-500 transition-colors"/>
      </div>
      <h3 className="font-bold text-slate-800 dark:text-slate-100 mb-2 group-hover:text-teal-600 dark:group-hover:text-teal-400 transition-colors">{resource.title}</h3>
      <p className="text-sm text-slate-500 dark:text-slate-400 flex-1 leading-relaxed">{resource.description}</p>
      <p className="text-xs text-slate-400 dark:text-slate-500 mt-4">{resource.viewCount.toLocaleString()} views</p>
    </a>
  );
};

export default ResourcesPage;
