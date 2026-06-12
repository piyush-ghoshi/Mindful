import { Download, AlertTriangle, CheckCircle, AlertCircle, Info, Leaf } from 'lucide-react';
import type { MentalHealthReportDto, SeverityLevel } from '../services/chatService';

interface Props {
  report: MentalHealthReportDto;
  userName?: string;
  onDownload?: () => void;
}

const SEVERITY_CONFIG: Record<SeverityLevel, {
  label: string; bar: string; bg: string; border: string;
  text: string; icon: React.ElementType; score: string;
}> = {
  LOW:      { label: 'Good',      bar: 'bg-emerald-500', bg: 'bg-emerald-50 dark:bg-emerald-900/20', border: 'border-emerald-200 dark:border-emerald-700/50', text: 'text-emerald-700 dark:text-emerald-300', icon: CheckCircle, score: 'text-emerald-600 dark:text-emerald-400' },
  MODERATE: { label: 'Fair',      bar: 'bg-amber-400',   bg: 'bg-amber-50 dark:bg-amber-900/20',    border: 'border-amber-200 dark:border-amber-700/50',    text: 'text-amber-700 dark:text-amber-300',   icon: Info,         score: 'text-amber-600 dark:text-amber-400' },
  HIGH:     { label: 'Needs Care',bar: 'bg-orange-500',  bg: 'bg-orange-50 dark:bg-orange-900/20',  border: 'border-orange-200 dark:border-orange-700/50',  text: 'text-orange-700 dark:text-orange-300', icon: AlertCircle,  score: 'text-orange-600 dark:text-orange-400' },
  SEVERE:   { label: 'Urgent',    bar: 'bg-red-500',     bg: 'bg-red-50 dark:bg-red-900/20',        border: 'border-red-200 dark:border-red-700/50',        text: 'text-red-700 dark:text-red-300',       icon: AlertTriangle,score: 'text-red-600 dark:text-red-400' },
};

const MindBotReport = ({ report, userName, onDownload }: Props) => {
  const cfg = SEVERITY_CONFIG[report.mentalStateLevel] ?? SEVERITY_CONFIG.LOW;
  const StatusIcon = cfg.icon;
  const date = new Date(report.createdAt).toLocaleDateString('en-IN', {
    day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit',
  });

  // Wellness score bar segments
  const scorePercent = report.wellnessScore;
  const barColor =
    scorePercent >= 70 ? 'bg-emerald-500' :
    scorePercent >= 45 ? 'bg-amber-400'   :
    scorePercent >= 25 ? 'bg-orange-500'  : 'bg-red-500';

  return (
    <div id="mindbot-report"
      className="bg-white dark:bg-slate-900 rounded-3xl border border-slate-100 dark:border-slate-700/50 overflow-hidden text-slate-800 dark:text-slate-200 max-w-2xl mx-auto shadow-xl">

      {/* ── Report header band ── */}
      <div className="relative bg-gradient-to-br from-[#006b5f] via-teal-700 to-slate-900 px-8 py-6 text-white overflow-hidden">
        <div className="absolute inset-0 opacity-10 pointer-events-none"
          style={{ backgroundImage: 'radial-gradient(circle at 80% -10%, white, transparent 50%)' }} />
        <div className="relative flex items-start justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-2 opacity-80">
              <Leaf size={14} />
              <span className="text-xs font-bold uppercase tracking-widest">Mindful · MindBot Report</span>
            </div>
            <h2 className="text-2xl font-black leading-tight">{report.title}</h2>
            <p className="text-teal-200/80 text-sm mt-1">{userName ?? 'User'} · {date}</p>
          </div>
          <div className="text-right flex-shrink-0">
            <div className={`text-4xl font-black ${cfg.score.replace('dark:text-', 'text-').split(' ')[0]} opacity-90`}>
              {report.wellnessScore}
            </div>
            <p className="text-teal-200/80 text-xs font-semibold mt-0.5">Wellness Score / 100</p>
          </div>
        </div>

        {/* Score bar */}
        <div className="mt-4 relative">
          <div className="h-2.5 bg-white/10 rounded-full overflow-hidden">
            <div className={`h-full rounded-full transition-all ${barColor}`}
              style={{ width: `${scorePercent}%` }} />
          </div>
          <div className="flex justify-between text-[10px] text-teal-200/60 mt-1.5 font-medium">
            <span>Severe</span><span>Needs Care</span><span>Fair</span><span>Good</span>
          </div>
        </div>
      </div>

      <div className="p-6 space-y-5">

        {/* ── Mental state indicator ── */}
        <div className={`flex items-center gap-3 p-4 rounded-2xl border ${cfg.bg} ${cfg.border}`}>
          <div className={`w-10 h-10 rounded-xl ${cfg.bg} border ${cfg.border} flex items-center justify-center flex-shrink-0`}>
            <StatusIcon size={20} className={cfg.text} />
          </div>
          <div>
            <p className={`font-bold text-sm ${cfg.text}`}>Mental State: {cfg.label}</p>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Level: {report.mentalStateLevel} · Score: {report.wellnessScore}/100
            </p>
          </div>
          {report.counsellorReferralSuggested && (
            <div className="ml-auto px-3 py-1.5 bg-red-100 dark:bg-red-900/40 text-red-700 dark:text-red-300 rounded-full text-xs font-bold border border-red-200 dark:border-red-700/50">
              Counsellor Recommended
            </div>
          )}
        </div>

        {/* ── Condition points ── */}
        <section>
          <h3 className="text-sm font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider mb-3 flex items-center gap-2">
            <span className="w-1.5 h-4 bg-teal-500 rounded-full inline-block" />
            Observed Condition
          </h3>
          <ul className="space-y-2">
            {report.conditionPoints.map((point, i) => (
              <li key={i} className="flex items-start gap-3 text-sm text-slate-600 dark:text-slate-400">
                <span className="mt-1.5 w-1.5 h-1.5 rounded-full bg-teal-500 flex-shrink-0" />
                {point}
              </li>
            ))}
          </ul>
        </section>

        <div className="grid grid-cols-2 gap-4">
          {/* ── Recommended exercises ── */}
          <section className="bg-emerald-50 dark:bg-emerald-900/15 rounded-2xl p-4 border border-emerald-100 dark:border-emerald-800/30">
            <h3 className="text-xs font-bold text-emerald-700 dark:text-emerald-400 uppercase tracking-wider mb-3">
              🏃 Exercises
            </h3>
            <ul className="space-y-1.5">
              {report.recommendedExercises.map((ex, i) => (
                <li key={i} className="text-xs text-slate-600 dark:text-slate-400 flex items-start gap-1.5">
                  <span className="mt-1 w-1 h-1 rounded-full bg-emerald-500 flex-shrink-0" />
                  {ex}
                </li>
              ))}
            </ul>
          </section>

          {/* ── Meditations ── */}
          <section className="bg-violet-50 dark:bg-violet-900/15 rounded-2xl p-4 border border-violet-100 dark:border-violet-800/30">
            <h3 className="text-xs font-bold text-violet-700 dark:text-violet-400 uppercase tracking-wider mb-3">
              🧘 Meditations
            </h3>
            <ul className="space-y-1.5">
              {report.recommendedMeditations.map((m, i) => (
                <li key={i} className="text-xs text-slate-600 dark:text-slate-400 flex items-start gap-1.5">
                  <span className="mt-1 w-1 h-1 rounded-full bg-violet-500 flex-shrink-0" />
                  {m}
                </li>
              ))}
            </ul>
          </section>
        </div>

        {/* ── Conclusion ── */}
        <section className="bg-slate-50 dark:bg-slate-800/50 rounded-2xl p-4 border border-slate-100 dark:border-slate-700/40">
          <h3 className="text-xs font-bold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-2">
            📋 Final Review
          </h3>
          <p className="text-sm text-slate-700 dark:text-slate-300 leading-relaxed">{report.conclusion}</p>
        </section>

        {/* ── Disclaimer + download ── */}
        <div className="border-t border-slate-100 dark:border-slate-700/50 pt-4 flex items-start justify-between gap-4">
          <p className="text-[10px] text-slate-400 dark:text-slate-500 leading-relaxed max-w-sm">
            ⚠️ <strong>Disclaimer:</strong> This is an AI-generated report for personal reference only. It is <em>not</em> a clinical diagnosis or professional counsellor report. Please consult a licensed mental health professional for medical advice. <strong>Mindful Watermark</strong> — Confidential.
          </p>
          {onDownload && (
            <button onClick={onDownload}
              className="flex items-center gap-2 px-4 py-2.5 bg-teal-500 hover:bg-teal-600 text-white rounded-xl text-xs font-bold transition-colors shadow-sm flex-shrink-0 whitespace-nowrap">
              <Download size={14} /> Download PDF
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default MindBotReport;
