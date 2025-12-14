import React from 'react';
import './App.css';
import Header from './components/Header';
import Footer from './components/Footer';

const App: React.FC = () => {
  return (
    <div className="app">
      <Header />
      <main>
        <h1>Главная страница</h1>
        <p>Здесь будет контент.</p>
      </main>
      <Footer />
    </div>
  );
};

export default App;
