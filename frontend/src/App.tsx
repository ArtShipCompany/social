import './App.css';
import CoopBoard from './components/CoopBoard/CoopBoard';
import Header from './components/Header/Header';
import Footer from './components/Footer/Footer';

const App = () => {
  return (
    <div className="app">
      <Header />
      <main>
        <div className="boards">
          <CoopBoard />
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default App;
