import React, { useState, useEffect, Suspense, lazy } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './i18n/i18n.config';
import { ROUTES } from './constants/routes';
import Sidebar from './components/layout/Sidebar';
import UsernameModal from './components/common/UsernameModal';
import LoadingSpinner from './components/common/LoadingSpinner';
import { usernameUtils } from './utils/usernameUtils';

// Lazy load pages for code splitting
const Home = lazy(() => import('./pages/Home'));
const IdiomGamePage = lazy(() => import('./pages/IdiomGamePage'));
const SentenceGamePage = lazy(() => import('./pages/SentenceGamePage'));
const LeaderboardPage = lazy(() => import('./pages/LeaderboardPage'));
const StatisticsPage = lazy(() => import('./pages/StatisticsPage'));

function App() {
  const [showUsernameModal, setShowUsernameModal] = useState(false);

  useEffect(() => {
    if (!usernameUtils.hasUsername()) {
      setShowUsernameModal(true);
    }
  }, []);

  const handleUsernameSubmit = (username: string) => {
    usernameUtils.setUsername(username);
    setShowUsernameModal(false);
  };

  const handleChangeUsername = () => {
    setShowUsernameModal(true);
  };

  const handleCloseUsernameModal = () => {
    setShowUsernameModal(false);
  };

  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  return (
    <Router>
      <UsernameModal
        show={showUsernameModal}
        onSubmit={handleUsernameSubmit}
        onClose={handleCloseUsernameModal}
      />
      <style>
        {`
          @media (max-width: 991px) {
            .main-content-responsive {
              margin-left: 0 !important;
            }
          }
        `}
      </style>
      <div className="d-flex min-vh-100 bg-light">
        <Sidebar onChangeUsername={handleChangeUsername} onCollapseChange={setSidebarCollapsed} />
        <div
          className="flex-grow-1 d-flex flex-column main-content-responsive"
          style={{
            marginLeft: sidebarCollapsed ? '60px' : '250px',
            transition: 'margin-left 0.3s ease',
          }}
        >
          <Suspense fallback={
            <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '100vh' }}>
              <LoadingSpinner size="large" message="加载中..." />
            </div>
          }>
            <Routes>
              <Route path={ROUTES.HOME} element={<Home />} />
              <Route path={ROUTES.IDIOM_GAME} element={<IdiomGamePage />} />
              <Route path={ROUTES.SENTENCE_GAME} element={<SentenceGamePage />} />
              <Route path={ROUTES.LEADERBOARD} element={<LeaderboardPage />} />
              <Route path={ROUTES.STATISTICS} element={<StatisticsPage />} />
            </Routes>
          </Suspense>
        </div>
      </div>
    </Router>
  );
}

export default App;
