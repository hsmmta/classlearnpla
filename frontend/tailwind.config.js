/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts,js}'],
  theme: {
    extend: {
      colors: {
        'brand-primary': '#5368DF',
        'brand-secondary': '#FA5757',
        'brand-dark': 'rgb(37,43,70)',
        'brand-muted': '#9194A1',
      },
      fontFamily: {
        sans: ['Inter', 'Microsoft YaHei', 'PingFang SC', 'sans-serif'],
      },
    },
  },
  plugins: [require('@tailwindcss/forms')],
}
