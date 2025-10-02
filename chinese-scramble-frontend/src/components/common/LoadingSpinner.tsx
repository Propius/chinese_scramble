import React from 'react';

interface LoadingSpinnerProps {
  size?: 'small' | 'medium' | 'large';
  message?: string;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = 'medium',
  message = '加载中...'
}) => {
  const sizeClasses = {
    small: 'w-6 h-6 border-2',
    medium: 'w-12 h-12 border-3',
    large: 'w-16 h-16 border-4'
  };

  const containerClasses = {
    small: 'text-sm',
    medium: 'text-base',
    large: 'text-lg'
  };

  return (
    <div className={`flex flex-col items-center justify-center gap-3 ${containerClasses[size]}`}>
      <div className={`${sizeClasses[size]} border-blue-200 border-t-blue-600 rounded-full animate-spin`}></div>
      {message && (
        <p className="text-gray-600 font-medium animate-pulse">{message}</p>
      )}
    </div>
  );
};

export default LoadingSpinner;
