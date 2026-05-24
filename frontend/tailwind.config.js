/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts,js}'],
  theme: {
    extend: {
      colors: {
        'brand-primary': '#0088FF',
        'brand-secondary': '#FA5757',
        'brand-dark': '#1F2937',
        'brand-muted': '#6B7280',
        'brand-success': '#4CAF50',
        'brand-warning': '#FF9800',
      },
      borderRadius: {
        xl: '0.75rem',
        '2xl': '1rem',
      },
      boxShadow: {
        card: '0 8px 24px rgba(15, 23, 42, 0.06)',
      },
      fontFamily: {
        sans: ['Inter', 'Microsoft YaHei', 'PingFang SC', 'sans-serif'],
      },
    },
  },
  plugins: [require('@tailwindcss/forms')],
}
