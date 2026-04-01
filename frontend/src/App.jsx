import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './layout/Header/Header'
import Footer from './layout/Footer/Footer'

import Home from './pages/Home/Home'

import Login from './pages/Login/Login'
import ForgotPassword from './pages/Login/ForgotPassword';
import ResetPassword from './pages/Login/ResetPassword';

import Register from './pages/Register/Register'
import VerificationSent from './pages/Register/VerificationSent'
import EmailVerification from './pages/Register/EmailVerification'

import ArtView from './pages/ArtView/ArtView'
import EditArt from './pages/ArtView/EditArt'
import CreateArt from './pages/ArtView/CreateArt'

import Profile from './pages/Profile/Profile'
import Me from './pages/Profile/Me'
import Edit from './pages/Profile/Edit'



import './App.css'

function App() {
  return (
    <Router>
      <div className="app">
        <Header />
        <div className="main">
          <Routes>
            <Route path="/" element={<Home />} />

            <Route path="/login" element={<Login />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />

            <Route path="/register" element={<Register />} />
            <Route path="/verify-email-sent" element={<VerificationSent />} />
            <Route path="/verify-email" element={<EmailVerification />} />

            <Route path="/me" element={<Me />} />
            <Route path="/edit" element={<Edit />} />
            <Route path="/profile/:userId" element={<Profile />} />

            <Route path="/art/:id" element={<ArtView />} />
            {/* EditArt - редактирование поста(лучше при рефакторинге переименовать PostView и EditPost) */}
            <Route path="/art/:id/edit" element={<EditArt />} />
            <Route path="/create" element={<CreateArt />} />
          </Routes>
        </div>
        <Footer />
      </div>
    </Router>
  );
}

export default App
