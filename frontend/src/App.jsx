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
import Follows from './pages/Follows/Follows';
import SettingsPage from './pages/SettingsPage/SettingsPage';

import Admin from './pages/Admin/Admin';
import ChangeUserRole from './pages/Admin/ChangeUserRole';
import AdminReports from './pages/Admin/AdminReports';

import Moderator from './pages/Moderator/Moderator';

import './App.css'
import Report from './pages/Report/Report';

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
            <Route path="/settings" element={<SettingsPage />} />
            <Route path="/profile/:userId" element={<Profile />} />
            <Route path="/follows/:userId?" element={<Follows />} />

            <Route path="/art/:id" element={<ArtView />} />
            {/* EditArt - редактирование поста(лучше при рефакторинге переименовать PostView и EditPost) */}
            
            <Route path="/art/:id/edit" element={<EditArt />} />
            <Route path="/create" element={<CreateArt />} />

            <Route path="/admin" element={<Admin />} />
            <Route path='/moderator' element={<Moderator />} />

            <Route path="/report/art/:artId" element={<Report />} />


          </Routes>
        </div>
        <Footer />
      </div>
    </Router>
  );
}

export default App
