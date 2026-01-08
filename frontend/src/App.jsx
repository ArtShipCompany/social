import { useState } from 'react'
import Header from './layout/Header/Header'
import Footer from './layout/Footer/Footer'
import Home from './pages/Home/Home'
import './App.css'

function App() {

  return (
    <div className="app">
      <Header />
        <div className="main">
          <Home />
        </div>
      <Footer />
    </div>
  )
}

export default App
