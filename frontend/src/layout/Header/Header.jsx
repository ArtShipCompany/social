import { Link, useLocation } from 'react-router-dom';
// import { useAuth } from '../context/AuthContext';
import styles from './Header.module.css';

function Header() {
    // const { user, logout } = useAuth();
    const location = useLocation()

    const handleLogout = () => {
        logout();
    };

    return (
        <div className={styles.header}>

            <Link to="/" className={styles.link}>
                <span className={styles.text}>ARTSHIP</span>
            </Link>

            <div className={styles.container}>
                {/* {!user ? (
                    <>
                        <Link to="/login" className={styles.coverBtn}>Войти</Link>
                        <Link to="/register" className={styles.coverBtn}>Зарегистрироваться</Link>
                    </>
                    ) : (
                    <>
                        {location.pathname === '/me' ? (
                            <button onClick={handleLogout} className={styles.logoutBtn}>
                                Выйти
                            </button>
                        ) : (
                            <Link to="/me" className={styles.avatarLink}>
                                <img 
                                    src={user.pfp || '/default-pfp.jpg'} 
                                    alt="Аватарка" 
                                />
                            </Link>
                        )}
                    </>
                )} */}
                <button className={styles.cover}>
                    Войти
                </button>
                <button className={styles.cover}>
                    Зарегистрироваться
                </button>
            </div>
        </div>
    );
}

export default Header;