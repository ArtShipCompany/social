import { useState } from 'react';
import { TEXTS } from '../../assets/texts';
import { useParams } from 'react-router-dom';
import { mockUsersMap } from '../../mock-images/mockArts';
import { mockArts } from '../../mock-images/mockArts';
import styles from './Profile.module.css';
import PFP from '../../assets/WA.jpg';
import artsIcon from '../../assets/arts-icon.svg';
import sms from '../../assets/message-icon.svg';

import ArtCard from '../../components/ArtCard/ArtCard';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';

export default function Profile() {
    const { userId } = useParams();
    const user = mockUsersMap[userId] || { id: userId, nickname: 'unknown', pfp: PFP };

    const [isSubscribed, setIsSubscribed] = useState(false);
    const toggleSubscribe = () => setIsSubscribed(!isSubscribed);

    return (
        <>
            <div className={styles.headContent}>
                <div className={styles.faceName}>
                    <img src={user.pfp} alt="profile-photo" className={styles.pfp}/>
                    <span className={styles.nickname}>@{user.nickname}</span>
                </div>

                <div className={styles.contentWrapper}>
                    <div className={styles.headBg}></div>

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

                    <div className={styles.headSFooter}>
                        <div className={styles.stats}>
                            <div className={styles.arts}>
                                <img src={artsIcon} alt="arts" />
                                <span>{' 111'}</span>
                            </div>
                            <span>Подписчики: {'5.5M'}</span>
                            <span>Подписки: {'505'}</span>
                        </div>

                        <div className={styles.bio}>
                            <span>Профиль пользователя: @{user.nickname}</span>
                        </div>

                        <div className={styles.status}>
                            <span>Статус: В сети</span>
                        </div>
                    </div>
                </div>
            </div>
            
            <div className={styles.feed}>
                {mockArts
                    .filter(art => art.ownerId === userId)
                    .map(art => (
                        <ArtCard key={art.id} id={art.id} image={art.image} typeShow={"full"} />
                    ))
                }
            </div>
        </>
    );
}