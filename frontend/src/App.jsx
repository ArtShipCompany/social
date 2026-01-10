import { useState } from 'react'
import Header from './layout/Header/Header'
import Footer from './layout/Footer/Footer'

import Home from './pages/Home/Home'
import Login from './pages/Login/Login'
import Register from './pages/Register/Register'

import Profile from './pages/Profile/Profile'
import Me from './pages/Profile/Me'
import Edit from './pages/Profile/Edit'

import './App.css'

function App() {

  return (
    <div className="app">
      <Header />
        <div className="main">
          <Edit />
        </div>
      <Footer />
    </div>
  )
}

export default App
