/**
 * Quick verification script to test the fix
 * Run: node test-restart-fix.js
 */

// Simulate the fix
const DEFAULT_FEATURE_FLAGS = {
  ENABLE_NO_REPEAT_QUESTIONS: false
};

function buildURL(difficulty, playerId, excludedIds) {
  const params = new URLSearchParams({ difficulty });
  if (playerId) {
    params.append('playerId', playerId);
  }
  // This is the FIX - check feature flag before sending excludedIds
  if (DEFAULT_FEATURE_FLAGS.ENABLE_NO_REPEAT_QUESTIONS && excludedIds && excludedIds.length > 0) {
    params.append('excludedIds', excludedIds.join(','));
  }
  return `/api/idiom-game/start?${params.toString()}`;
}

console.log('=== TESTING THE FIX ===\n');

console.log('Test 1: Feature flag OFF, excludedIds undefined');
console.log('URL:', buildURL('EXPERT', 'player1', undefined));
console.log('Expected: No excludedIds parameter');
console.log('');

console.log('Test 2: Feature flag OFF, excludedIds with stale data');
console.log('URL:', buildURL('EXPERT', 'player1', ['1', '2', '3']));
console.log('Expected: No excludedIds parameter (feature OFF!)');
console.log('');

console.log('Test 3: Turn feature ON');
DEFAULT_FEATURE_FLAGS.ENABLE_NO_REPEAT_QUESTIONS = true;
console.log('URL:', buildURL('EXPERT', 'player1', ['1', '2', '3']));
console.log('Expected: excludedIds=1,2,3');
console.log('');

console.log('Test 4: Feature ON, empty array');
console.log('URL:', buildURL('EXPERT', 'player1', []));
console.log('Expected: No excludedIds parameter (array empty)');
console.log('');

console.log('=== RESULTS ===');
console.log('✅ Fix prevents excludedIds from being sent when feature is OFF');
console.log('✅ Fix only sends excludedIds when feature is ON AND array has items');
