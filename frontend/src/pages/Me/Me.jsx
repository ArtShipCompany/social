import { useState } from 'react';

import styles from './Me.module.css';
import PFP from '../../assets/WA.jpg';
import sms from '../../assets/message-icon.svg';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';

export default function Me() {
    const [isSubscribed, setIsSubscribed] = useState(false);

    const toggleSubscribe = () => {
        setIsSubscribed(!isSubscribed);
    };

    return (
        <>
            <div className={styles.headContent}>
                <div className={styles.headBg}></div>

                <div className={styles.faceName}>
                    <img src={PFP} alt="profile-photo" className={styles.pfp}/>
                    <span className={styles.nickname}>@
                        <span className={styles.link}>some_name</span>
                    </span>
                </div>
                <div className={styles.buttonsCover}>
                    <DefaultBtn
                    text={isSubscribed ? 'Подписка' : 'Подписаться'}
                    onClick={toggleSubscribe}
                    className={`${styles.subscribe} ${isSubscribed ? styles.subscribed : ''}`}
                    />
                    <button className={styles.message}>
                    <img src={sms} alt="sms" className={styles.icon} />
                    </button>
                </div>               
            </div>

        </>
    );
}