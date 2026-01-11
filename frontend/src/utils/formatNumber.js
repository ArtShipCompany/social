export const formatNumber = (num) => {
  if (num < 1000) {
    return num.toString();
  }

  let formatted = (num / 1000).toFixed(1);

  if (formatted.endsWith('.0')) {
    formatted = formatted.slice(0, -2);
  }

  return formatted + 'к';
};