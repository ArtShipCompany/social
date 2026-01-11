import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useState } from 'react'
import Header from './layout/Header/Header'
import Footer from './layout/Footer/Footer'

import Home from './pages/Home/Home'
import Login from './pages/Login/Login'
import Register from './pages/Register/Register'

import ArtView from './pages/ArtView/ArtView'
import EditArt from './pages/ArtView/EditArt'

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
            <Route path="/register" element={<Register />} />
            <Route path="/me" element={<Me />} />
            <Route path="edit" element={<Edit />} />
            <Route path="/profile/:userId" element={<Profile />} />
            <Route path="/art/:id" element={<ArtView />} />
            <Route path="/art/:id/edit" element={<EditArt />} />
          </Routes>
        </div>
        <Footer />
      </div>
    </Router>
  );
}

export default App
