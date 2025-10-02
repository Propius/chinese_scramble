import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ROUTES } from '../../constants/routes';
import { usernameUtils } from '../../utils/usernameUtils';

interface SidebarProps {
  onChangeUsername?: () => void;
  onCollapseChange?: (collapsed: boolean) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ onChangeUsername, onCollapseChange }) => {
  const { t } = useTranslation();
  const location = useLocation();
  const [isMobileOpen, setIsMobileOpen] = useState(false);
  const [isDesktopCollapsed, setIsDesktopCollapsed] = useState(false);

  const navItems = [
    { path: ROUTES.HOME, label: t('navigation.home'), icon: '🏠' },
    { path: ROUTES.IDIOM_GAME, label: t('navigation.idiomGame'), icon: '🎯' },
    { path: ROUTES.SENTENCE_GAME, label: t('navigation.sentenceGame'), icon: '📝' },
    { path: ROUTES.LEADERBOARD, label: t('navigation.leaderboard'), icon: '🏆' },
    { path: ROUTES.STATISTICS, label: t('navigation.statistics'), icon: '📊' },
  ];

  const toggleMobileSidebar = () => setIsMobileOpen(!isMobileOpen);
  const toggleDesktopCollapse = () => {
    const newCollapsedState = !isDesktopCollapsed;
    setIsDesktopCollapsed(newCollapsedState);
    if (onCollapseChange) {
      onCollapseChange(newCollapsedState);
    }
  };

  const sidebarWidth = isDesktopCollapsed ? '60px' : '250px';

  return (
    <>
      {/* Mobile Toggle Button */}
      <button
        className="btn btn-primary position-fixed d-lg-none"
        style={{
          top: '1rem',
          left: '1rem',
          zIndex: 1060,
          width: '3rem',
          height: '3rem',
          padding: '0',
        }}
        onClick={toggleMobileSidebar}
        aria-label="Toggle sidebar"
      >
        {isMobileOpen ? '✕' : '☰'}
      </button>

      {/* Desktop Collapse Toggle Button */}
      <button
        className="btn btn-light position-fixed d-none d-lg-block"
        style={{
          top: '1rem',
          left: isDesktopCollapsed ? '0.5rem' : '200px',
          zIndex: 1060,
          width: '2.5rem',
          height: '2.5rem',
          padding: '0',
          border: '1px solid #dee2e6',
          borderRadius: '8px',
          transition: 'left 0.3s ease',
          boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
        }}
        onClick={toggleDesktopCollapse}
        aria-label="Toggle sidebar collapse"
        title={isDesktopCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
      >
        {isDesktopCollapsed ? '☰' : '◀'}
      </button>

      {/* Overlay for mobile */}
      {isMobileOpen && (
        <div
          className="position-fixed d-lg-none"
          style={{
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            zIndex: 1050,
          }}
          onClick={toggleMobileSidebar}
        />
      )}

      {/* Sidebar */}
      <div
        className={`d-flex flex-column position-fixed ${isMobileOpen ? '' : 'd-none d-lg-flex'}`}
        style={{
          top: 0,
          left: 0,
          bottom: 0,
          width: sidebarWidth,
          zIndex: 1055,
          boxShadow: '2px 0 8px rgba(0, 0, 0, 0.1)',
          overflowY: 'auto',
          overflowX: 'hidden',
          background: '#ffffff',
          transition: 'width 0.3s ease',
        }}
      >
        {/* Brand */}
        <div className="p-3 border-bottom" style={{ borderColor: '#e5e7eb' }}>
          <Link
            to={ROUTES.HOME}
            className="text-dark text-decoration-none d-flex align-items-center gap-2"
            onClick={() => setIsMobileOpen(false)}
            style={{ whiteSpace: 'nowrap', justifyContent: isDesktopCollapsed ? 'center' : 'flex-start' }}
          >
            <span style={{ fontSize: '1.5rem', flexShrink: 0 }}>🀄</span>
            {!isDesktopCollapsed && <h5 className="mb-0 fw-bold">汉字游戏</h5>}
          </Link>
        </div>

        {/* Navigation Links */}
        <nav style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: '0.5rem', minHeight: 0 }}>
          <ul className="list-unstyled mb-0">
            {navItems.map((item) => (
              <li key={item.path} className="mb-1">
                <Link
                  to={item.path}
                  className={`d-flex align-items-center gap-3 px-3 py-2 text-decoration-none ${
                    location.pathname === item.path
                      ? 'text-primary fw-semibold'
                      : 'text-dark'
                  }`}
                  style={{
                    transition: 'all 0.2s',
                    fontSize: '0.95rem',
                    borderRadius: '8px',
                    backgroundColor: location.pathname === item.path ? '#dbeafe' : 'transparent',
                    justifyContent: isDesktopCollapsed ? 'center' : 'flex-start',
                  }}
                  onClick={() => setIsMobileOpen(false)}
                  title={isDesktopCollapsed ? item.label : ''}
                >
                  <span style={{ fontSize: '1.25rem', flexShrink: 0 }}>{item.icon}</span>
                  {!isDesktopCollapsed && <span>{item.label}</span>}
                </Link>
              </li>
            ))}
          </ul>
        </nav>

        {/* User Profile Section */}
        <div className="p-3 border-top" style={{ borderColor: '#e5e7eb' }}>
          {!isDesktopCollapsed && (
            <div>
              <div className="text-xs text-gray-600 mb-1" style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                当前用户:
              </div>
              <div className="font-bold text-gray-800 mb-2" style={{ fontWeight: '600', fontSize: '0.9rem' }}>
                {usernameUtils.getUsername() || 'Guest'}
              </div>
              {onChangeUsername && (
                <button
                  className="btn btn-sm btn-outline-primary w-100"
                  style={{
                    fontSize: '0.8rem',
                    padding: '0.25rem 0.5rem',
                    border: '1px solid #0d6efd',
                    color: '#0d6efd',
                    backgroundColor: 'transparent',
                    borderRadius: '0.25rem',
                    textDecoration: 'none'
                  }}
                  onClick={onChangeUsername}
                >
                  更改用户名
                </button>
              )}
            </div>
          )}
          {isDesktopCollapsed && (
            <button
              className="btn btn-sm btn-outline-primary w-100 p-1"
              title="用户设置"
              onClick={onChangeUsername}
              style={{ fontSize: '1.25rem', padding: '0.25rem' }}
            >
              👤
            </button>
          )}
        </div>

        {/* Footer */}
        <div className="p-3 border-top text-center" style={{ borderColor: '#e5e7eb', color: '#6b7280' }}>
          {!isDesktopCollapsed && <small>© 2025 汉字游戏</small>}
          {isDesktopCollapsed && <small>©</small>}
        </div>
      </div>
    </>
  );
};

export default Sidebar;
