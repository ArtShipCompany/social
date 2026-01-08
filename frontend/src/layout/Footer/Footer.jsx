import styles from './Footer.module.css';

function Footer() {
    return (
        <div className={styles.footer}>

            <a className={styles.link} href="/">
                <span className={styles.text}>ARTSHIP</span>
            </a>

        </div>
    );
}

export default Footer;