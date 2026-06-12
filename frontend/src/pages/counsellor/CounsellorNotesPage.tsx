import { useState } from 'react';
import { FileText, Plus, Save } from 'lucide-react';

interface Note { id: string; studentId: string; content: string; date: string; }

const CounsellorNotesPage = () => {
  const [notes, setNotes] = useState<Note[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [studentId, setStudentId] = useState('');
  const [content, setContent] = useState('');

  const addNote = () => {
    if (!content.trim()) return;
    setNotes(p => [...p, { id: Date.now().toString(), studentId: studentId || 'General', content, date: new Date().toISOString() }]);
    setContent(''); setStudentId(''); setShowForm(false);
  };

  const inputCls = 'w-full bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-3 text-sm text-slate-800 dark:text-slate-100 placeholder:text-slate-400 dark:placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-teal-500 transition-all';

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-800 dark:text-white flex items-center gap-3">
            <FileText size={28} className="text-amber-500" /> Session Notes
          </h1>
          <p className="text-slate-500 dark:text-slate-400 mt-1">Private clinical notes for your sessions</p>
        </div>
        <button onClick={() => setShowForm(true)}
          className="flex items-center gap-2 px-4 py-2.5 bg-teal-600 hover:bg-teal-500 text-white rounded-xl text-sm font-semibold transition-colors shadow-sm">
          <Plus size={16} /> Add Note
        </button>
      </div>

      {showForm && (
        <div className="bg-white dark:bg-slate-800/60 rounded-2xl border border-teal-300/50 dark:border-teal-600/40 p-6">
          <h2 className="font-bold text-slate-800 dark:text-white mb-4">New Session Note</h2>
          <div className="space-y-3">
            <input type="text" value={studentId} onChange={e => setStudentId(e.target.value)}
              placeholder="Student ID (optional)" className={inputCls} />
            <textarea value={content} onChange={e => setContent(e.target.value)}
              placeholder="Write your clinical notes here…" rows={5}
              className={`${inputCls} resize-none`} />
            <div className="flex gap-3">
              <button onClick={() => setShowForm(false)}
                className="flex-1 py-2.5 rounded-xl border border-slate-200 dark:border-slate-700 text-sm font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors">
                Cancel
              </button>
              <button onClick={addNote} disabled={!content.trim()}
                className="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl bg-teal-600 hover:bg-teal-500 text-white text-sm font-semibold transition-colors disabled:opacity-50">
                <Save size={15} /> Save Note
              </button>
            </div>
          </div>
        </div>
      )}

      {notes.length === 0 ? (
        <div className="bg-white dark:bg-slate-800/60 rounded-2xl p-12 border border-slate-100 dark:border-slate-700/50 text-center">
          <FileText size={40} className="text-slate-300 dark:text-slate-600 mx-auto mb-3" />
          <p className="text-slate-500 dark:text-slate-400 font-medium">No notes yet</p>
          <p className="text-slate-400 dark:text-slate-500 text-sm mt-1">Add clinical notes after each session.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {notes.map(note => (
            <div key={note.id} className="bg-white dark:bg-slate-800/60 rounded-2xl p-5 border border-slate-100 dark:border-slate-700/50">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-bold text-slate-700 dark:text-slate-200">
                  {note.studentId !== 'General' ? `Student: ${note.studentId}` : 'General Note'}
                </span>
                <span className="text-xs text-slate-400">
                  {new Date(note.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                </span>
              </div>
              <p className="text-sm text-slate-600 dark:text-slate-300 leading-relaxed whitespace-pre-wrap">{note.content}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default CounsellorNotesPage;
