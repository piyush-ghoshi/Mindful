import { useState, useEffect } from 'react';
import { TrendingUp, Target, Award, Plus, CheckCircle, Circle, Flame, Trash2 } from 'lucide-react';
import { wellnessService, type WellnessGoal } from '../services/wellnessService';

const BADGES = [
  { id:'1', name:'First Check-in', desc:'Logged your first mood entry', icon:'🌱', unlocked:false },
  { id:'2', name:'7-Day Streak',   desc:'Checked in 7 days in a row',   icon:'🔥', unlocked:false },
  { id:'3', name:'First Session',  desc:'Completed your first session', icon:'💬', unlocked:false },
  { id:'4', name:'Goal Setter',    desc:'Created your first goal',      icon:'🎯', unlocked:false },
  { id:'5', name:'Resource Reader',desc:'Read 5 wellness articles',     icon:'📚', unlocked:false },
  { id:'6', name:'Mindful Month',  desc:'Checked in every day for a month', icon:'🏆', unlocked:false },
];

const input = 'w-full bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-teal-500 transition-all';

const WellnessTrackerPage = () => {
  const [goals, setGoals] = useState<WellnessGoal[]>([]);
  const [showAddGoal, setShowAddGoal] = useState(false);
  const [newGoalTitle, setNewGoalTitle] = useState('');
  const [newGoalDate, setNewGoalDate] = useState('');

  useEffect(() => { setGoals(wellnessService.getGoals()); }, []);

  const addGoal = () => {
    if (!newGoalTitle.trim()) return;
    const goal = wellnessService.addGoal(
      newGoalTitle.trim(),
      newGoalDate || new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
    );
    setGoals(prev => [goal, ...prev]);
    setNewGoalTitle(''); setNewGoalDate(''); setShowAddGoal(false);
  };

  const toggleGoal = (id: string) => {
    const goal = goals.find(g => g.id === id);
    if (!goal) return;
    const updates = { status: (goal.status === 'COMPLETED' ? 'ACTIVE' : 'COMPLETED') as WellnessGoal['status'], progress: goal.status === 'COMPLETED' ? 50 : 100 };
    wellnessService.updateGoal(id, updates);
    setGoals(prev => prev.map(g => g.id === id ? { ...g, ...updates } : g));
  };

  const updateProgress = (id: string, progress: number) => {
    wellnessService.updateGoal(id, { progress });
    setGoals(prev => prev.map(g => g.id === id ? { ...g, progress } : g));
  };

  const deleteGoal = (id: string) => {
    wellnessService.deleteGoal(id);
    setGoals(prev => prev.filter(g => g.id !== id));
  };

  const activeGoals    = goals.filter(g => g.status === 'ACTIVE').length;
  const completedGoals = goals.filter(g => g.status === 'COMPLETED').length;
  const hasGoal        = goals.length > 0;
  const badgesUnlocked = completedGoals > 0 ? [{ ...BADGES[3], unlocked: true }] : [];
  const allBadges      = BADGES.map(b => badgesUnlocked.find(u => u.id === b.id) ?? b);

  const stats = [
    { label:'Active Goals',      value:activeGoals.toString(),    icon:Target,    color:'text-sky-600 dark:text-sky-400',    bg:'bg-sky-500/10 dark:bg-sky-500/20' },
    { label:'Goals Completed',   value:completedGoals.toString(), icon:TrendingUp,color:'text-teal-600 dark:text-teal-400',  bg:'bg-teal-500/10 dark:bg-teal-500/20' },
    { label:'Wellness Points',   value: hasGoal ? String(completedGoals * 50) : '0', icon:Flame,     color:'text-orange-500 dark:text-orange-400',bg:'bg-orange-500/10 dark:bg-orange-500/20' },
    { label:'Badges Earned',     value:badgesUnlocked.length.toString(), icon:Award,  color:'text-amber-500 dark:text-amber-400', bg:'bg-amber-500/10 dark:bg-amber-500/20' },
  ];

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-3">
          <TrendingUp size={28} className="text-teal-500"/>Wellness Tracker
        </h1>
        <p className="text-slate-500 dark:text-slate-400 mt-1">Track your progress and celebrate your wins</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        {stats.map(({ label, value, icon:Icon, color, bg }) => (
          <div key={label} className="bg-white dark:bg-slate-800/60 rounded-2xl p-5 border border-slate-100 dark:border-slate-700/50">
            <div className={`w-10 h-10 rounded-xl ${bg} flex items-center justify-center mb-3`}>
              <Icon size={20} className={color}/>
            </div>
            <p className={`text-2xl font-bold ${color}`}>{value}</p>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{label}</p>
          </div>
        ))}
      </div>

      {/* Level */}
      <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-slate-100 dark:border-slate-700/50">
        <div className="flex items-center justify-between mb-3">
          <div>
            <h2 className="text-lg font-bold text-slate-800 dark:text-white">
              Level {Math.floor(completedGoals / 3) + 1} — {['Seedling 🌱','Sapling 🌿','Tree 🌳','Forest 🌲','Zen Master 🧘'][Math.min(Math.floor(completedGoals / 3), 4)]}
            </h2>
            <p className="text-sm text-slate-500 dark:text-slate-400">{completedGoals % 3} / 3 goals to next level</p>
          </div>
          <span className="text-3xl">{['🌱','🌿','🌳','🌲','🧘'][Math.min(Math.floor(completedGoals / 3), 4)]}</span>
        </div>
        <div className="w-full bg-slate-100 dark:bg-slate-700 rounded-full h-3">
          <div className="bg-teal-500 h-3 rounded-full transition-all" style={{ width: `${((completedGoals % 3) / 3) * 100}%` }}/>
        </div>
        <p className="text-xs text-slate-400 dark:text-slate-500 mt-2">Complete goals to earn points and level up.</p>
      </div>

      {/* Goals */}
      <section>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold text-slate-800 dark:text-white flex items-center gap-2">
            <Target size={20} className="text-sky-500"/>Wellness Goals
          </h2>
          <button onClick={() => setShowAddGoal(true)}
            className="flex items-center gap-2 px-4 py-2 bg-teal-500 hover:bg-teal-600 text-white rounded-xl text-sm font-semibold transition-colors shadow-sm">
            <Plus size={16}/>Add Goal
          </button>
        </div>

        {showAddGoal && (
          <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-6 border border-teal-300/50 dark:border-teal-600/40 mb-4">
            <h3 className="font-bold text-slate-800 dark:text-white mb-4">New Wellness Goal</h3>
            <div className="space-y-3">
              <input type="text" value={newGoalTitle} onChange={e => setNewGoalTitle(e.target.value)}
                placeholder="e.g., Meditate for 10 minutes daily" className={input}/>
              <input type="date" value={newGoalDate} min={new Date().toISOString().split('T')[0]}
                onChange={e => setNewGoalDate(e.target.value)} className={input}/>
              <div className="flex gap-3">
                <button onClick={() => setShowAddGoal(false)}
                  className="flex-1 py-2.5 rounded-xl border border-slate-200 dark:border-slate-700 text-sm font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors">
                  Cancel
                </button>
                <button onClick={addGoal} disabled={!newGoalTitle.trim()}
                  className="flex-1 py-2.5 rounded-xl bg-teal-500 hover:bg-teal-600 text-white text-sm font-semibold transition-colors disabled:opacity-50">
                  Add Goal
                </button>
              </div>
            </div>
          </div>
        )}

        {goals.length === 0 ? (
          <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-10 border border-slate-100 dark:border-slate-700/50 text-center">
            <Target size={40} className="text-slate-300 dark:text-slate-600 mx-auto mb-3"/>
            <p className="text-slate-500 dark:text-slate-400 font-medium mb-1">No goals yet</p>
            <p className="text-slate-400 dark:text-slate-500 text-sm">Set your first wellness goal to start tracking your progress.</p>
          </div>
        ) : (
          <div className="space-y-3">
            {goals.map(goal => (
              <div key={goal.id} className="bg-white dark:bg-slate-800/60 rounded-2xl p-5 border border-slate-100 dark:border-slate-700/50">
                <div className="flex items-start gap-3">
                  <button onClick={() => toggleGoal(goal.id)} className="mt-0.5 flex-shrink-0">
                    {goal.status === 'COMPLETED'
                      ? <CheckCircle size={22} className="text-teal-500"/>
                      : <Circle size={22} className="text-slate-300 dark:text-slate-600"/>}
                  </button>
                  <div className="flex-1 min-w-0">
                    <p className={`font-semibold ${goal.status==='COMPLETED' ? 'line-through text-slate-400 dark:text-slate-600' : 'text-slate-800 dark:text-slate-100'}`}>
                      {goal.title}
                    </p>
                    <p className="text-xs text-slate-400 dark:text-slate-500 mt-0.5">
                      Target: {new Date(goal.targetDate).toLocaleDateString('en-US',{month:'short',day:'numeric',year:'numeric'})}
                    </p>
                    <div className="mt-3">
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-xs text-slate-500 dark:text-slate-400">Progress</span>
                        <span className="text-xs font-bold text-teal-600 dark:text-teal-400">{goal.progress}%</span>
                      </div>
                      <input type="range" min={0} max={100} value={goal.progress}
                        onChange={e => updateProgress(goal.id, Number(e.target.value))}
                        disabled={goal.status==='COMPLETED'}
                        className="w-full h-2 rounded-full appearance-none cursor-pointer disabled:opacity-50"
                        style={{ background:`linear-gradient(to right, #14b8a6 ${goal.progress}%, #334155 ${goal.progress}%)` }}
                      />
                    </div>
                  </div>
                  <button onClick={() => deleteGoal(goal.id)} className="p-1.5 rounded-lg hover:bg-red-50 dark:hover:bg-red-900/20 text-slate-400 hover:text-red-500 transition-colors flex-shrink-0">
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* Badges */}
      <section>
        <h2 className="text-xl font-bold text-slate-800 dark:text-white mb-4 flex items-center gap-2">
          <Award size={20} className="text-amber-500"/>Achievements
        </h2>
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-3">
          {allBadges.map(badge => (
            <div key={badge.id}
              className={`rounded-2xl p-4 border text-center transition-all ${
                badge.unlocked
                  ? 'bg-amber-50 dark:bg-amber-900/20 border-amber-200 dark:border-amber-700/50'
                  : 'bg-white dark:bg-slate-800/40 border-slate-100 dark:border-slate-700/40 opacity-40 grayscale'
              }`}>
              <div className="text-3xl mb-2">{badge.icon}</div>
              <p className="text-xs font-bold text-slate-800 dark:text-slate-200 leading-tight">{badge.name}</p>
              <p className="text-xs text-slate-400 dark:text-slate-500 mt-1 leading-tight">{badge.desc}</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};

export default WellnessTrackerPage;
