import { Link } from 'react-router-dom';
import {
  Heart, Shield, Users, Award, Brain, Globe,
  ArrowRight, CheckCircle, Sparkles,
} from 'lucide-react';

const team = [
  { name: 'Dr. Aisha Verma',    role: 'Chief Wellness Officer',    initials: 'AV', color: 'from-teal-400 to-emerald-500',   bio: 'Ph.D. in Clinical Psychology · 12 years in student mental health' },
  { name: 'Rohan Mehta',        role: 'Co-Founder & CTO',          initials: 'RM', color: 'from-violet-400 to-purple-500',  bio: 'IIT Delhi · Building ethical health-tech platforms since 2015' },
  { name: 'Dr. Priya Sharma',   role: 'Head of Counselling',       initials: 'PS', color: 'from-sky-400 to-blue-500',      bio: 'M.D. Psychiatry · Specialised in adolescent & campus wellness' },
  { name: 'Ananya Singh',       role: 'Student Experience Lead',   initials: 'AS', color: 'from-amber-400 to-orange-500',  bio: 'Former student counsellor · Advocate for accessible mental health' },
];

const values = [
  { icon: Heart,   title: 'Empathy First',      desc: 'Every feature is designed around compassion — students deserve to feel heard, not processed.',         color: 'text-rose-500',   bg: 'bg-rose-500/10 dark:bg-rose-500/20' },
  { icon: Shield,  title: 'Privacy by Design',  desc: 'Your data is yours. End-to-end privacy, zero selling of personal information. Ever.',                   color: 'text-teal-500',   bg: 'bg-teal-500/10 dark:bg-teal-500/20' },
  { icon: Brain,   title: 'Evidence-Based',     desc: 'All our resources, tools and matching algorithms are grounded in clinical research.',                    color: 'text-violet-500', bg: 'bg-violet-500/10 dark:bg-violet-500/20' },
  { icon: Globe,   title: 'Accessible to All',  desc: 'Multi-language support, screen-reader friendly, and free tiers for students who can\'t afford care.',   color: 'text-sky-500',    bg: 'bg-sky-500/10 dark:bg-sky-500/20' },
  { icon: Users,   title: 'Community Driven',   desc: 'Our peer forum and support groups are moderated by trained volunteers who have lived experience.',        color: 'text-amber-500',  bg: 'bg-amber-500/10 dark:bg-amber-500/20' },
  { icon: Award,   title: 'Verified Experts',   desc: 'Every counsellor on the platform is licensed, background-checked, and trained in campus mental health.', color: 'text-emerald-500',bg: 'bg-emerald-500/10 dark:bg-emerald-500/20' },
];

const stats = [
  { value: '12,000+', label: 'Students Supported' },
  { value: '94%',     label: 'Felt Better After 4 Sessions' },
  { value: '180+',    label: 'Verified Counsellors' },
  { value: '4.8 / 5', label: 'Average Platform Rating' },
];

const AboutPage = () => (
  <div className="space-y-24 pb-24">

    {/* ── Hero ── */}
    <section className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#006b5f] via-teal-700 to-slate-900 px-8 py-20 text-white text-center">
      <div className="absolute inset-0 opacity-10 pointer-events-none"
        style={{ backgroundImage: 'radial-gradient(circle at 20% 50%, #3bbfad 0%, transparent 50%), radial-gradient(circle at 80% 20%, #6366f1 0%, transparent 40%)' }} />
      <div className="relative z-10 max-w-3xl mx-auto">
        <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-white/10 border border-white/20 text-teal-200 text-xs font-semibold uppercase tracking-widest mb-6">
          <Sparkles size={14} /> Our Story
        </div>
        <h1 className="text-5xl font-black leading-tight mb-6">
          Mental wellness is not a luxury —<br />
          <span className="text-teal-300">it's a student right.</span>
        </h1>
        <p className="text-teal-100/80 text-lg leading-relaxed max-w-2xl mx-auto">
          Mindful was founded in 2022 by a group of students, counsellors and technologists who watched campus mental health resources collapse under demand. We built the platform we wished had existed.
        </p>
      </div>
    </section>

    {/* ── Stats ── */}
    <section className="grid grid-cols-2 md:grid-cols-4 gap-6 max-w-4xl mx-auto">
      {stats.map(({ value, label }) => (
        <div key={label} className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50 text-center shadow-sm">
          <p className="text-3xl font-black text-teal-600 dark:text-teal-400 mb-1">{value}</p>
          <p className="text-sm text-slate-500 dark:text-slate-400 font-medium">{label}</p>
        </div>
      ))}
    </section>

    {/* ── Mission ── */}
    <section className="max-w-4xl mx-auto grid md:grid-cols-2 gap-12 items-center">
      <div>
        <p className="text-xs font-bold uppercase tracking-widest text-teal-600 dark:text-teal-400 mb-3">Our Mission</p>
        <h2 className="text-4xl font-black text-slate-800 dark:text-white mb-6 leading-tight">
          Making professional mental health support as normal as going to the library.
        </h2>
        <p className="text-slate-600 dark:text-slate-400 leading-relaxed mb-6">
          We believe every student — regardless of background, budget, or location — deserves access to compassionate, evidence-based mental health care. Mindful connects students to verified counsellors, peer communities, and self-care tools in one seamless platform.
        </p>
        <ul className="space-y-3">
          {['No waitlists. Book a session in under 2 minutes.', 'Counsellors verified and background-checked.', '100% HIPAA-compliant data handling.', 'Scholarships available for underserved students.'].map(item => (
            <li key={item} className="flex items-start gap-3 text-sm text-slate-600 dark:text-slate-400">
              <CheckCircle size={16} className="text-teal-500 flex-shrink-0 mt-0.5" />
              {item}
            </li>
          ))}
        </ul>
      </div>
      <div className="bg-gradient-to-br from-teal-500/10 to-violet-500/10 dark:from-teal-500/20 dark:to-violet-500/20 rounded-3xl p-8 border border-teal-200/50 dark:border-teal-700/30">
        <div className="text-6xl mb-4">🌱</div>
        <blockquote className="text-xl font-bold text-slate-800 dark:text-white italic leading-relaxed mb-4">
          "We don't just connect students to counsellors. We help students understand that asking for help is the bravest thing they can do."
        </blockquote>
        <p className="text-sm text-slate-500 dark:text-slate-400 font-semibold">— Dr. Aisha Verma, Co-Founder</p>
      </div>
    </section>

    {/* ── Values ── */}
    <section className="max-w-5xl mx-auto">
      <div className="text-center mb-12">
        <p className="text-xs font-bold uppercase tracking-widest text-teal-600 dark:text-teal-400 mb-3">What We Stand For</p>
        <h2 className="text-4xl font-black text-slate-800 dark:text-white">Our core values</h2>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {values.map(({ icon: Icon, title, desc, color, bg }) => (
          <div key={title} className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50 hover:shadow-lg hover:shadow-black/5 dark:hover:shadow-black/20 transition-all">
            <div className={`w-12 h-12 rounded-xl ${bg} flex items-center justify-center mb-4`}>
              <Icon size={22} className={color} />
            </div>
            <h3 className="text-base font-bold text-slate-800 dark:text-white mb-2">{title}</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400 leading-relaxed">{desc}</p>
          </div>
        ))}
      </div>
    </section>

    {/* ── Team ── */}
    <section className="max-w-5xl mx-auto">
      <div className="text-center mb-12">
        <p className="text-xs font-bold uppercase tracking-widest text-teal-600 dark:text-teal-400 mb-3">The People Behind Mindful</p>
        <h2 className="text-4xl font-black text-slate-800 dark:text-white">Our team</h2>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {team.map(({ name, role, initials, color, bio }) => (
          <div key={name} className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50 text-center">
            <div className={`w-16 h-16 rounded-2xl bg-gradient-to-br ${color} flex items-center justify-center text-white text-2xl font-bold mx-auto mb-4 shadow-lg`}>
              {initials}
            </div>
            <h3 className="font-bold text-slate-800 dark:text-white text-sm mb-1">{name}</h3>
            <p className="text-xs text-teal-600 dark:text-teal-400 font-semibold mb-3">{role}</p>
            <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed">{bio}</p>
          </div>
        ))}
      </div>
    </section>

    {/* ── CTA ── */}
    <section className="max-w-3xl mx-auto text-center">
      <div className="bg-gradient-to-br from-teal-500 to-emerald-600 rounded-3xl p-12 text-white shadow-2xl shadow-teal-500/20">
        <h2 className="text-3xl font-black mb-4">Ready to start your journey?</h2>
        <p className="text-teal-100 mb-8 text-lg">Join thousands of students who have already taken the first step.</p>
        <div className="flex items-center justify-center gap-4 flex-wrap">
          <Link to="/register" className="px-8 py-3.5 bg-white text-teal-700 rounded-xl font-bold hover:bg-teal-50 transition-colors flex items-center gap-2 shadow-lg">
            Get Started Free <ArrowRight size={18} />
          </Link>
          <Link to="/login" className="px-8 py-3.5 bg-white/10 border border-white/30 text-white rounded-xl font-bold hover:bg-white/20 transition-colors">
            Sign In
          </Link>
        </div>
      </div>
    </section>

  </div>
);

export default AboutPage;
