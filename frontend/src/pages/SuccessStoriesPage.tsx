import { Star, Quote, TrendingUp, Heart, Users, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';

const stories = [
  {
    name: 'Priya R.',
    course: 'B.Tech CSE, 3rd Year',
    avatar: 'PR',
    color: 'from-teal-400 to-emerald-500',
    rating: 5,
    before: 'I was having panic attacks before every exam. I couldn\'t sleep, couldn\'t focus, and was too embarrassed to tell anyone.',
    after: 'After 6 sessions with my counsellor on Mindful, I learned breathing techniques and cognitive reframing. My last semester was my best ever — 9.1 CGPA.',
    improvement: 'Anxiety Management',
    weeks: 8,
    tag: 'Exam Anxiety',
  },
  {
    name: 'Arjun M.',
    course: 'MBA, 1st Year',
    avatar: 'AM',
    color: 'from-violet-400 to-purple-500',
    rating: 5,
    before: 'Moving to a new city for MBA felt crushing. I had no friends, felt like an imposter in every class, and stopped eating properly.',
    after: 'Mindful\'s peer forum connected me with other students going through the same thing. My counsellor helped me build routines. Now I\'m the most active member of our batch.',
    improvement: 'Depression & Isolation',
    weeks: 12,
    tag: 'Loneliness',
  },
  {
    name: 'Sneha K.',
    course: 'MBBS, 2nd Year',
    avatar: 'SK',
    color: 'from-rose-400 to-pink-500',
    rating: 5,
    before: 'Medical school is brutal. I was constantly comparing myself to peers and felt worthless when I scored lower. I was working 18-hour days and burning out.',
    after: 'My counsellor helped me realise I was in a toxic achievement loop. I now have boundaries around study time, and my mental health is no longer negotiable.',
    improvement: 'Burnout Recovery',
    weeks: 10,
    tag: 'Academic Burnout',
  },
  {
    name: 'Rahul T.',
    course: 'B.Sc Psychology, 4th Year',
    avatar: 'RT',
    color: 'from-amber-400 to-orange-500',
    rating: 5,
    before: 'I struggled with self-harm for two years and never told anyone. I found Mindful during a particularly dark week and reached out through the crisis support.',
    after: 'The crisis team responded within minutes. I was connected with a trauma-specialist counsellor. It\'s been 14 months clean and I volunteer as a peer mentor now.',
    improvement: 'Crisis Recovery',
    weeks: 20,
    tag: 'Mental Health Crisis',
  },
  {
    name: 'Meera S.',
    course: 'M.A. Literature, 2nd Year',
    avatar: 'MS',
    color: 'from-sky-400 to-blue-500',
    rating: 5,
    before: 'My parents\' divorce happened during my thesis submission month. I was paralysed — couldn\'t write a single word for 3 weeks.',
    after: 'Grief counselling through Mindful helped me process the loss and compartmentalise. I submitted my thesis on time and got a distinction.',
    improvement: 'Grief & Life Events',
    weeks: 7,
    tag: 'Family Stress',
  },
  {
    name: 'Kabir N.',
    course: 'B.Com Finance, 2nd Year',
    avatar: 'KN',
    color: 'from-emerald-400 to-teal-500',
    rating: 5,
    before: 'Social anxiety made presentations a nightmare. I would skip classes just to avoid being called on. My attendance was at risk.',
    after: 'CBT sessions on Mindful, combined with gradual exposure exercises, transformed how I see social situations. I gave a 20-minute presentation last week — and enjoyed it.',
    improvement: 'Social Anxiety',
    weeks: 9,
    tag: 'Social Anxiety',
  },
];

const tagColors: Record<string, string> = {
  'Exam Anxiety':      'bg-teal-100 text-teal-700 dark:bg-teal-900/40 dark:text-teal-300',
  'Loneliness':        'bg-violet-100 text-violet-700 dark:bg-violet-900/40 dark:text-violet-300',
  'Academic Burnout':  'bg-amber-100 text-amber-700 dark:bg-amber-900/40 dark:text-amber-300',
  'Mental Health Crisis':'bg-rose-100 text-rose-700 dark:bg-rose-900/40 dark:text-rose-300',
  'Family Stress':     'bg-sky-100 text-sky-700 dark:bg-sky-900/40 dark:text-sky-300',
  'Social Anxiety':    'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/40 dark:text-emerald-300',
};

const SuccessStoriesPage = () => (
  <div className="space-y-20 pb-24">

    {/* ── Hero ── */}
    <section className="text-center max-w-3xl mx-auto pt-8">
      <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-teal-500/10 dark:bg-teal-500/20 border border-teal-200 dark:border-teal-700/50 text-teal-700 dark:text-teal-300 text-xs font-semibold uppercase tracking-widest mb-6">
        <Heart size={14} className="fill-current" /> Real Students · Real Change
      </div>
      <h1 className="text-5xl font-black text-slate-800 dark:text-white mb-6 leading-tight">
        Stories of <span className="text-teal-600 dark:text-teal-400">transformation</span>
      </h1>
      <p className="text-lg text-slate-600 dark:text-slate-400 leading-relaxed">
        These are real accounts shared with permission from students who have used Mindful. Names have been partially anonymised to protect privacy. We share them so others know — you are not alone, and things can get better.
      </p>
    </section>

    {/* ── Stats strip ── */}
    <section className="grid grid-cols-3 gap-6 max-w-2xl mx-auto">
      {[
        { icon: Users,      value: '12,000+', label: 'Students helped' },
        { icon: TrendingUp, value: '94%',      label: 'Reported improvement' },
        { icon: Star,       value: '4.8 / 5',  label: 'Average rating' },
      ].map(({ icon: Icon, value, label }) => (
        <div key={label} className="bg-white dark:bg-slate-800/60 rounded-2xl p-5 border border-slate-100 dark:border-slate-700/50 text-center">
          <Icon size={20} className="text-teal-500 mx-auto mb-2" />
          <p className="text-2xl font-black text-slate-800 dark:text-white">{value}</p>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">{label}</p>
        </div>
      ))}
    </section>

    {/* ── Stories ── */}
    <section className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-8">
      {stories.map(({ name, course, avatar, color, rating, before, after, improvement, weeks, tag }) => (
        <article key={name} className="bg-white dark:bg-slate-800/60 rounded-3xl border border-slate-100 dark:border-slate-700/50 overflow-hidden hover:shadow-xl hover:shadow-black/5 dark:hover:shadow-black/20 transition-all group">
          {/* Header */}
          <div className={`bg-gradient-to-br ${color} p-6`}>
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 rounded-2xl bg-white/20 backdrop-blur border border-white/30 flex items-center justify-center text-white text-xl font-black">
                {avatar}
              </div>
              <div>
                <p className="font-black text-white text-lg">{name}</p>
                <p className="text-white/80 text-xs font-medium">{course}</p>
                <div className="flex gap-0.5 mt-1.5">
                  {Array.from({ length: rating }).map((_, i) => (
                    <Star key={i} size={12} className="text-white fill-white" />
                  ))}
                </div>
              </div>
              <div className="ml-auto text-right">
                <p className="text-white/90 text-xs font-semibold">{weeks} weeks</p>
                <p className="text-white/70 text-[10px]">of sessions</p>
              </div>
            </div>
          </div>

          {/* Body */}
          <div className="p-6 space-y-4">
            <span className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${tagColors[tag] ?? 'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300'}`}>{tag}</span>

            <div className="space-y-3">
              <div>
                <p className="text-xs font-bold text-rose-500 dark:text-rose-400 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
                  <Quote size={12} /> Before
                </p>
                <p className="text-sm text-slate-600 dark:text-slate-400 leading-relaxed italic">"{before}"</p>
              </div>
              <div className="border-t border-slate-100 dark:border-slate-700/50 pt-3">
                <p className="text-xs font-bold text-teal-600 dark:text-teal-400 uppercase tracking-wider mb-1.5 flex items-center gap-1.5">
                  <TrendingUp size={12} /> After Mindful
                </p>
                <p className="text-sm text-slate-700 dark:text-slate-300 leading-relaxed">"{after}"</p>
              </div>
            </div>

            <div className="pt-2 border-t border-slate-100 dark:border-slate-700/50 flex items-center justify-between">
              <span className="text-xs text-slate-400 dark:text-slate-500">Key improvement area</span>
              <span className="text-xs font-bold text-teal-700 dark:text-teal-300">{improvement}</span>
            </div>
          </div>
        </article>
      ))}
    </section>

    {/* ── Disclaimer ── */}
    <section className="max-w-3xl mx-auto bg-slate-50 dark:bg-slate-800/40 rounded-2xl p-6 border border-slate-200 dark:border-slate-700/50 text-center">
      <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed">
        <strong className="text-slate-700 dark:text-slate-300">Privacy note:</strong> All stories are shared with explicit written consent. Names, courses, and identifying details have been partially anonymised. Results vary — mental health progress is personal and non-linear. Mindful does not guarantee specific outcomes.
      </p>
    </section>

    {/* ── CTA ── */}
    <section className="max-w-3xl mx-auto text-center">
      <div className="bg-gradient-to-br from-teal-500 to-emerald-600 rounded-3xl p-12 text-white">
        <h2 className="text-3xl font-black mb-4">Your story could be next.</h2>
        <p className="text-teal-100 mb-8">Thousands of students have already taken the first step. It starts with a single session.</p>
        <Link to="/register" className="inline-flex items-center gap-2 px-8 py-4 bg-white text-teal-700 rounded-xl font-bold hover:bg-teal-50 transition-colors shadow-lg text-lg">
          Start Your Journey <ArrowRight size={20} />
        </Link>
      </div>
    </section>

  </div>
);

export default SuccessStoriesPage;
