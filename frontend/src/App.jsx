import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useState } from 'react'
import Header from './layout/Header/Header'
import Footer from './layout/Footer/Footer'

import Home from './pages/Home/Home'
import Login from './pages/Login/Login'
import Register from './pages/Register/Register'

import ArtView from './pages/ArtView/ArtView'
import EditArt from './pages/ArtView/EditArt'
import CreateArt from './pages/ArtView/CreateArt'

import Profile from './pages/Profile/Profile'
import Me from './pages/Profile/Me'
import Edit from './pages/Profile/Edit'



import './App.css'

// ПОДСКАЗКА как на данный момент попасть на стр. Me или Edit с нее:
// иди на Register -> нажми внизу на "Есть аккаунт?" >Войти<
// потом просто поменяешь на странице там есть коммент

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
            {/* Edit - редактирование профиля */}
            <Route path="edit" element={<Edit />} />
            <Route path="/profile/:userId" element={<Profile />} />

            <Route path="/art/:id" element={<ArtView />} />
            {/* EditArt - редактирование поста(лучше при рефакторинге переименовать PostView и EditPost) */}
            <Route path="/art/:id/edit" element={<EditArt />} />
            <Route path="/create-art" element={<CreateArt />} />
          </Routes>
        </div>
        <Footer />
      </div>
    </Router>
  );
}

export default App
