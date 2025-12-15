import './App.css';
import Header from './components/Header/Header';
import Footer from './components/Footer/Footer';

const App = () => {
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
