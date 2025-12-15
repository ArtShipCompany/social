import './App.css';
import CoopBoard from './components/CoopBoard/CoopBoard';
import PrivateBoard from 'components/PrivateBoard/PrivateBoard';
import Header from './components/Header/Header';
import Footer from './components/Footer/Footer';
import SearchBar from 'components/SerchBar/SearchBar';
import Feed from 'components/Feed/Feed';

const App = () => {
  return (
    <div className="app">
      <Header />
      <main>
        <div className="boards">
          <PrivateBoard />
          <CoopBoard />
        </div>
        <SearchBar />
        <Feed />
      </main>
      <Footer />
    </div>
  );
};

export default App;
