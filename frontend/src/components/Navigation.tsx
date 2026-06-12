import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../styles/Navigation.css';

/**
 * Navigation Component
 */
const Navigation: React.FC = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout, loading } = useAuth();
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  /**
   * Handle logout
   */
  const handleLogout = async () => {
    try {
      await logout();
      navigate('/login');
    } catch (error) {
      console.error('Logout error:', error);
    }
  };

  /**
   * Toggle mobile menu
   */
  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen);
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-logo">
          🧠 Mindful
        </Link>

        <button className="menu-toggle" onClick={toggleMenu}>
          ☰
        </button>

        <div className={`navbar-menu ${isMenuOpen ? 'active' : ''}`}>
          {isAuthenticated ? (
            <>
              <Link to="/dashboard" className="nav-link">
                Dashboard
              </Link>
              <Link to="/mood-tracking" className="nav-link">
                Mood Tracking
              </Link>
              <Link to="/appointments" className="nav-link">
                Appointments
              </Link>
              <Link to="/resources" className="nav-link">
                Resources
              </Link>

              <div className="navbar-user">
                <span className="user-name">
                  {user?.firstName} {user?.lastName}
                </span>
                <Link to="/profile" className="nav-link">
                  Profile
                </Link>
                <button
                  onClick={handleLogout}
                  disabled={loading}
                  className="logout-button"
                >
                  {loading ? 'Logging out...' : 'Logout'}
                </button>
              </div>
            </>
          ) : (
            <>
              <Link to="/login" className="nav-link">
                Login
              </Link>
              <Link to="/register" className="nav-link register-link">
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navigation;
