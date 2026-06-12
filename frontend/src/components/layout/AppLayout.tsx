import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import TopNav from './TopNav';
import SOSButton from '../SOSButton';

const AppLayout = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="min-h-screen flex flex-col bg-[#f0faf8] dark:bg-[#0b1221] transition-colors duration-300">
      <TopNav onMenuToggle={() => setSidebarOpen(o => !o)} />

      <div className="flex flex-1 overflow-hidden">
        <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />

        {sidebarOpen && (
          <div className="fixed inset-0 bg-black/30 backdrop-blur-sm z-20 md:hidden"
            onClick={() => setSidebarOpen(false)} />
        )}

        <main className="flex-1 overflow-y-auto px-4 md:px-8 py-6 md:py-8">
          <div className="max-w-5xl mx-auto">
            <Outlet />
          </div>
        </main>
      </div>

      <SOSButton />
    </div>
  );
};

export default AppLayout;
