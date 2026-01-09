import { useState } from 'react'
import Header from './layout/Header/Header'
import Footer from './layout/Footer/Footer'

import Login from './pages/Login/Login'
import Register from './pages/Register/Register'
import Home from './pages/Home/Home'
import './App.css'

function App() {

  return (
    <div className="app">
      <Header />
        <div className="main">
          <Register />
        </div>
      <Footer />
    </div>
  )
}

export default App
