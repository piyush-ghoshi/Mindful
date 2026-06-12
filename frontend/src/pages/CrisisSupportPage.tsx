import { Phone, MessageCircle, Globe, AlertTriangle, Heart, Shield, Clock } from 'lucide-react';

const HOTLINES = [
  { name:'iCall (India)',          number:'9152987821',    description:'Psychosocial helpline by TISS. Mon–Sat, 8am–10pm.', available:'8am – 10pm' },
  { name:'Vandrevala Foundation',  number:'1860-2662-345', description:'24/7 mental health helpline across India.',          available:'24/7' },
  { name:'NIMHANS Helpline',       number:'080-46110007',  description:'National Institute of Mental Health helpline.',      available:'24/7' },
  { name:'Snehi',                  number:'044-24640050',  description:'Emotional support and suicide prevention helpline.', available:'24/7' },
];

const COPING = [
  { icon:Heart,  title:'Breathing Exercise', desc:'Box breathing: Inhale 4s → Hold 4s → Exhale 4s → Hold 4s. Repeat 4 times.', bg:'bg-pink-500/10 dark:bg-pink-500/20', color:'text-pink-600 dark:text-pink-300' },
  { icon:Shield, title:'Grounding Technique', desc:'5-4-3-2-1: Name 5 things you see, 4 you can touch, 3 you hear, 2 you smell, 1 you taste.', bg:'bg-sky-500/10 dark:bg-sky-500/20', color:'text-sky-600 dark:text-sky-300' },
  { icon:Clock,  title:'Take a Break', desc:'Step away from the situation. A 10-minute walk can significantly reduce stress hormones.', bg:'bg-teal-500/10 dark:bg-teal-500/20', color:'text-teal-600 dark:text-teal-300' },
];

const CrisisSupportPage = () => (
  <div className="space-y-8">
    {/* Emergency banner */}
    <div className="relative rounded-2xl p-8 overflow-hidden bg-gradient-to-br from-red-600 to-rose-700 dark:from-red-800 dark:to-rose-900 border border-red-500/30 shadow-lg shadow-red-500/20">
      <div className="absolute -right-8 -top-8 w-40 h-40 bg-white/10 rounded-full blur-3xl pointer-events-none"/>
      <div className="relative z-10">
        <div className="flex items-center gap-3 mb-3">
          <AlertTriangle size={28} className="text-red-200"/>
          <h1 className="text-2xl font-bold text-white">Crisis Support</h1>
        </div>
        <p className="text-red-100 text-base leading-relaxed max-w-2xl">
          If you or someone you know is in immediate danger, please call emergency services (112) immediately. You are not alone — help is available right now.
        </p>
        <a href="tel:112"
          className="inline-flex items-center gap-2 mt-5 bg-white text-red-700 font-bold px-6 py-3 rounded-xl hover:bg-red-50 transition-colors shadow-sm">
          <Phone size={18}/>Call Emergency: 112
        </a>
      </div>
    </div>

    {/* Coping tools */}
    <section>
      <h2 className="text-xl font-bold text-slate-800 dark:text-white mb-4">Immediate Coping Tools</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {COPING.map(({ icon:Icon, title, desc, bg, color }) => (
          <div key={title} className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
            <div className={`w-12 h-12 rounded-2xl ${bg} flex items-center justify-center mb-4`}>
              <Icon size={22} className={color}/>
            </div>
            <h3 className="font-bold text-slate-800 dark:text-slate-100 mb-2">{title}</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400 leading-relaxed">{desc}</p>
          </div>
        ))}
      </div>
    </section>

    {/* Helplines */}
    <section>
      <h2 className="text-xl font-bold text-slate-800 dark:text-white mb-4 flex items-center gap-2">
        <Phone size={20} className="text-teal-500"/>Crisis Helplines
      </h2>
      <div className="space-y-3">
        {HOTLINES.map(h => (
          <div key={h.name}
            className="bg-white dark:bg-slate-800/60 rounded-2xl p-5 border border-slate-100 dark:border-slate-700/50 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-2xl bg-teal-500/10 dark:bg-teal-500/20 flex items-center justify-center flex-shrink-0">
                <Phone size={20} className="text-teal-600 dark:text-teal-400"/>
              </div>
              <div>
                <h3 className="font-bold text-slate-800 dark:text-slate-100">{h.name}</h3>
                <p className="text-sm text-slate-500 dark:text-slate-400">{h.description}</p>
                <div className="flex items-center gap-1.5 mt-1">
                  <Clock size={12} className="text-slate-400"/>
                  <span className="text-xs text-slate-400 dark:text-slate-500">{h.available}</span>
                </div>
              </div>
            </div>
            <a href={`tel:${h.number.replace(/-/g,'')}`}
              className="flex items-center gap-2 px-5 py-2.5 bg-teal-500 hover:bg-teal-600 text-white rounded-xl font-semibold text-sm transition-colors whitespace-nowrap shadow-sm">
              <Phone size={14}/>{h.number}
            </a>
          </div>
        ))}
      </div>
    </section>

    {/* Online */}
    <section>
      <h2 className="text-xl font-bold text-slate-800 dark:text-white mb-4 flex items-center gap-2">
        <Globe size={20} className="text-sky-500"/>Online Support
      </h2>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {[
          { title:'iCall Online Counselling', desc:'Book an online session with a trained counsellor.', url:'https://icallhelpline.org', icon:MessageCircle },
          { title:'NIMHANS Resources', desc:"Access mental health resources from India's premier institute.", url:'https://nimhans.ac.in', icon:Globe },
        ].map(({ title, desc, url, icon:Icon }) => (
          <a key={title} href={url} target="_blank" rel="noopener noreferrer"
            className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50 hover:shadow-md hover:border-teal-300/50 dark:hover:border-teal-600/50 transition-all group flex items-start gap-4">
            <div className="w-12 h-12 rounded-2xl bg-teal-500/10 dark:bg-teal-500/20 flex items-center justify-center flex-shrink-0 group-hover:bg-teal-500/20 dark:group-hover:bg-teal-500/30 transition-colors">
              <Icon size={22} className="text-teal-600 dark:text-teal-400"/>
            </div>
            <div>
              <h3 className="font-bold text-slate-800 dark:text-slate-100 group-hover:text-teal-600 dark:group-hover:text-teal-400 transition-colors">{title}</h3>
              <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">{desc}</p>
            </div>
          </a>
        ))}
      </div>
    </section>

    {/* Reminder */}
    <div className="bg-teal-500/10 dark:bg-teal-900/30 border border-teal-200 dark:border-teal-700/50 rounded-2xl p-6 text-center">
      <p className="text-teal-700 dark:text-teal-300 font-bold text-lg mb-1">You are not alone.</p>
      <p className="text-slate-600 dark:text-slate-400 text-sm">Reaching out for help is a sign of strength. Our counsellors are here to support you.</p>
    </div>
  </div>
);

export default CrisisSupportPage;
