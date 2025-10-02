import React from 'react';

interface HeaderProps {
  title: string;
  subtitle?: string;
  variant?: 'standalone' | 'card';
}

export const Header: React.FC<HeaderProps> = ({ title, subtitle, variant = 'card' }) => {
  if (variant === 'card') {
    // Card variant - no wrapper, just content for use inside a card
    return (
      <div style={{ padding: '2rem 1.5rem 0.75rem 1.5rem', textAlign: 'center' }}>
        <h1 className="text-3xl font-bold text-gray-900">{title}</h1>
        {subtitle && <p className="mt-2 text-sm text-gray-600">{subtitle}</p>}
      </div>
    );
  }

  // Standalone variant - original style
  return (
    <header className="bg-white shadow">
      <div className="container mx-auto px-4 py-6 text-center">
        <h1 className="text-3xl font-bold text-gray-900">{title}</h1>
        {subtitle && <p className="mt-2 text-sm text-gray-600">{subtitle}</p>}
      </div>
    </header>
  );
};

export default Header;
