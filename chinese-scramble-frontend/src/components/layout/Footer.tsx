import React from 'react';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-gray-800 text-white mt-auto">
      <div className="container mx-auto px-4 py-6">
        <div className="text-center">
          <p className="text-sm">© 2025 汉字游戏. 版权所有.</p>
          <p className="text-xs text-gray-400 mt-2">Chinese Word Scramble Game</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
