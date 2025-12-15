import './App.css';
import CoopBoard from './components/CoopBoard/CoopBoard';
import Header from './components/Header/Header';
import Footer from './components/Footer/Footer';

const App = () => {
  return (
    <div className="app">
      <Header />
      <main>
        <CoopBoard />
        <h1>Главная страница</h1>
        <p>Здесь будет контент.</p>
      </main>
      <Footer />
    </div>
  );
};

export default App;
