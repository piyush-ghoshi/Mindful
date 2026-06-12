import { useState } from 'react';
import { moodService } from '../services/moodService';
import type { MoodEntry } from '../types';
import { BookOpen, TrendingUp, Calendar, ChevronDown, ChevronUp } from 'lucide-react';

const EMOTIONS = [
  { id: 'happy',   emoji: '😊', label: 'Happy'   },
  { id: 'calm',    emoji: '😌', label: 'Calm'    },
  { id: 'neutral', emoji: '😐', label: 'Neutral' },
  { id: 'anxious', emoji: '😰', label: 'Anxious' },
  { id: 'sad',     emoji: '😢', label: 'Sad'     },
  { id: 'stressed',emoji: '😫', label: 'Stressed'},
  { id: 'angry',   emoji: '😠', label: 'Angry'   },
  { id: 'excited', emoji: '🤩', label: 'Excited' },
];

const MOOD_LABELS: Record<number, string> = {
  1:'Very Low',2:'Low',3:'Below Average',4:'Fair',5:'Okay',
  6:'Good',7:'Pretty Good',8:'Great',9:'Excellent',10:'Amazing',
};

const MOOD_COLORS: Record<number, string> = {
  1:'#ef4444',2:'#f97316',3:'#f59e0b',4:'#eab308',5:'#84cc16',
  6:'#22c55e',7:'#10b981',8:'#14b8a6',9:'#06b6d4',10:'#3b82f6',
};

const input = 'w-full bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-teal-500 dark:focus:ring-teal-400 transition-all';

const MoodJournalPage = () => {
  const [moodRating, setMoodRating] = useState(7);
  const [selectedEmotions, setSelectedEmotions] = useState<string[]>([]);
  const [energyLevel, setEnergyLevel] = useState(3);
  const [sleepQuality, setSleepQuality] = useState(3);
  const [journalText, setJournalText] = useState('');
  const [triggers, setTriggers] = useState('');
  const [activities, setActivities] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState('');
  const [history, setHistory] = useState<MoodEntry[]>([]);
  const [showHistory, setShowHistory] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);

  const toggleEmotion = (id: string) =>
    setSelectedEmotions(p => p.includes(id) ? p.filter(e => e !== id) : [...p, id]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedEmotions.length) { setError('Please select at least one emotion'); return; }
    try {
      setSubmitting(true); setError('');
      // Backend accepts moodRating 1-5; UI uses 1-10 scale → map down
      const backendRating = Math.max(1, Math.min(5, Math.round(moodRating / 2)));
      await moodService.recordMoodEntry({
        moodRating: backendRating,
        emotions: selectedEmotions,
        energyLevel,
        sleepQuality,
        journalText: journalText.trim() || undefined,
        triggers: triggers.split(',').map(t => t.trim()).filter(Boolean),
        activities: activities.split(',').map(a => a.trim()).filter(Boolean),
        recordedAt: new Date().toISOString(),
      });
      setSuccess(true);
      setSelectedEmotions([]); setJournalText(''); setTriggers(''); setActivities('');
      setMoodRating(7); setEnergyLevel(3); setSleepQuality(3);
      setTimeout(() => setSuccess(false), 4000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save mood entry');
    } finally { setSubmitting(false); }
  };

  const loadHistory = async () => {
    if (showHistory) { setShowHistory(false); return; }
    try {
      setLoadingHistory(true);
      const res = await moodService.getMoodHistory(30);
      setHistory((res.entries ?? []).map(e => ({
        ...e,
        // map backend 1-5 rating back to 1-10 for display
        moodRating: e.moodRating * 2,
      })));
      setShowHistory(true);
    } catch { setHistory([]); setShowHistory(true); }
    finally { setLoadingHistory(false); }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-3">
            <BookOpen size={28} className="text-teal-500" />
            Mood Journal
          </h1>
          <p className="text-slate-500 dark:text-slate-400 mt-1">Track how you're feeling today</p>
        </div>
        <button
          onClick={loadHistory}
          className="flex items-center gap-2 px-4 py-2 rounded-xl border border-slate-200 dark:border-slate-700 text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors"
        >
          <Calendar size={16} />
          {loadingHistory ? 'Loading…' : showHistory ? 'Hide History' : 'View History'}
          {showHistory ? <ChevronUp size={14}/> : <ChevronDown size={14}/>}
        </button>
      </div>

      {/* History */}
      {showHistory && (
        <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
          <h2 className="text-lg font-semibold text-slate-800 dark:text-white mb-4 flex items-center gap-2">
            <TrendingUp size={18} className="text-teal-500"/>Last 30 Days
          </h2>
          {history.length === 0 ? (
            <p className="text-slate-400 dark:text-slate-500 text-sm text-center py-6">No mood entries yet. Start tracking below!</p>
          ) : (
            <div className="space-y-3 max-h-64 overflow-y-auto">
              {history.map(entry => (
                <div key={entry.id} className="flex items-center justify-between p-3 rounded-xl bg-slate-50 dark:bg-slate-700/50">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full flex items-center justify-center text-white font-bold text-sm"
                      style={{ backgroundColor: MOOD_COLORS[entry.moodRating] ?? '#14b8a6' }}>
                      {entry.moodRating}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-slate-800 dark:text-slate-200">{entry.emotions.join(', ')}</p>
                      <p className="text-xs text-slate-400">{new Date(entry.recordedAt).toLocaleDateString('en-US',{weekday:'short',month:'short',day:'numeric'})}</p>
                    </div>
                  </div>
                  {entry.journalText && <p className="text-xs text-slate-500 dark:text-slate-400 max-w-xs truncate hidden sm:block">{entry.journalText}</p>}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Check-in form */}
      <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-8 border border-slate-100 dark:border-slate-700/50 shadow-sm">
        <h2 className="text-xl font-bold text-slate-800 dark:text-white mb-6">Daily Check-In</h2>

        {success && <div className="mb-6 p-4 rounded-xl bg-teal-50 dark:bg-teal-900/30 border border-teal-200 dark:border-teal-700 text-teal-700 dark:text-teal-300 text-sm font-medium">✅ Mood entry saved! Keep it up.</div>}
        {error  && <div className="mb-6 p-4 rounded-xl bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-700 text-red-700 dark:text-red-300 text-sm">{error}</div>}

        <form onSubmit={handleSubmit} className="space-y-8">
          {/* Mood slider */}
          <div>
            <div className="flex items-center justify-between mb-3">
              <label className="text-sm font-bold text-slate-700 dark:text-slate-200">Overall Mood</label>
              <div className="flex items-center gap-2">
                <span className="text-3xl font-bold" style={{ color: MOOD_COLORS[moodRating] }}>{moodRating}</span>
                <span className="text-sm text-slate-500 dark:text-slate-400">{MOOD_LABELS[moodRating]}</span>
              </div>
            </div>
            <input type="range" min={1} max={10} value={moodRating}
              onChange={e => setMoodRating(Number(e.target.value))}
              className="w-full h-2 rounded-full appearance-none cursor-pointer"
              style={{ background: `linear-gradient(to right, ${MOOD_COLORS[moodRating]} ${(moodRating-1)*11.1}%, #334155 ${(moodRating-1)*11.1}%)` }}
            />
            <div className="flex justify-between text-xs text-slate-400 mt-1"><span>Very Low</span><span>Amazing</span></div>
          </div>

          {/* Emotions */}
          <div>
            <label className="text-sm font-bold text-slate-700 dark:text-slate-200 block mb-3">
              How are you feeling? <span className="text-slate-400 font-normal">(select all that apply)</span>
            </label>
            <div className="grid grid-cols-4 sm:grid-cols-8 gap-2">
              {EMOTIONS.map(({ id, emoji, label }) => (
                <button key={id} type="button" onClick={() => toggleEmotion(id)}
                  className={`flex flex-col items-center gap-1 p-3 rounded-xl border-2 transition-all ${
                    selectedEmotions.includes(id)
                      ? 'border-teal-500 bg-teal-50 dark:bg-teal-900/40 dark:border-teal-400'
                      : 'border-slate-100 dark:border-slate-700 hover:border-slate-300 dark:hover:border-slate-500 bg-white dark:bg-slate-800/40'
                  }`}>
                  <span className="text-2xl">{emoji}</span>
                  <span className="text-xs text-slate-600 dark:text-slate-300 font-medium">{label}</span>
                </button>
              ))}
            </div>
          </div>

          {/* Energy + Sleep */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            {[
              { label:'Energy Level', value:energyLevel, setter:setEnergyLevel, labels:['Drained','Low','Moderate','High','Energized'] },
              { label:'Sleep Quality', value:sleepQuality, setter:setSleepQuality, labels:['Very Poor','Poor','Fair','Good','Excellent'] },
            ].map(({ label, value, setter, labels }) => (
              <div key={label}>
                <div className="flex items-center justify-between mb-2">
                  <label className="text-sm font-bold text-slate-700 dark:text-slate-200">{label}</label>
                  <span className="text-sm text-teal-600 dark:text-teal-400 font-medium">{labels[value-1]}</span>
                </div>
                <div className="flex gap-2">
                  {[1,2,3,4,5].map(v => (
                    <button key={v} type="button" onClick={() => setter(v)}
                      className={`flex-1 h-9 rounded-xl text-sm font-bold transition-all ${
                        v <= value
                          ? 'bg-teal-500 dark:bg-teal-600 text-white shadow-sm'
                          : 'bg-slate-100 dark:bg-slate-700/60 text-slate-400 dark:text-slate-500 hover:bg-slate-200 dark:hover:bg-slate-700'
                      }`}>{v}</button>
                  ))}
                </div>
              </div>
            ))}
          </div>

          {/* Journal */}
          <div>
            <label className="text-sm font-bold text-slate-700 dark:text-slate-200 block mb-2">
              Journal Notes <span className="text-slate-400 font-normal">(optional)</span>
            </label>
            <textarea value={journalText} onChange={e => setJournalText(e.target.value)}
              placeholder="Write about your day, thoughts, or feelings…"
              maxLength={500} rows={4}
              className={`${input} resize-none`}
            />
            <p className="text-xs text-slate-400 text-right mt-1">{journalText.length}/500</p>
          </div>

          {/* Triggers + Activities */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {[
              { label:'Triggers', value:triggers, setter:setTriggers, placeholder:'e.g., work stress, lack of sleep' },
              { label:'Activities', value:activities, setter:setActivities, placeholder:'e.g., exercise, meditation' },
            ].map(({ label, value, setter, placeholder }) => (
              <div key={label}>
                <label className="text-sm font-bold text-slate-700 dark:text-slate-200 block mb-2">
                  {label} <span className="text-slate-400 font-normal">(comma-separated)</span>
                </label>
                <input type="text" value={value} onChange={e => setter(e.target.value)} placeholder={placeholder} className={input}/>
              </div>
            ))}
          </div>

          <button type="submit" disabled={submitting}
            className="w-full bg-teal-500 hover:bg-teal-600 dark:bg-teal-600 dark:hover:bg-teal-500 text-white font-bold py-4 rounded-xl transition-all disabled:opacity-60 disabled:cursor-not-allowed text-base shadow-lg shadow-teal-500/20">
            {submitting ? 'Saving…' : 'Save Mood Entry'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default MoodJournalPage;
