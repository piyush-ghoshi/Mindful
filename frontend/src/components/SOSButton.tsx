import { useNavigate } from 'react-router-dom';

const SOSButton = () => {
  const navigate = useNavigate();

  return (
    <button
      onClick={() => navigate('/crisis-support')}
      className="fixed bottom-8 right-8 w-14 h-14 bg-[#ba1a1a] rounded-full shadow-lg flex items-center justify-center text-white font-bold text-sm hover:bg-red-700 hover:scale-105 transition-all z-50"
      aria-label="Emergency SOS"
    >
      SOS
    </button>
  );
};

export default SOSButton;
