import { useState } from 'react';
import { mockArts } from '../../mock-images/mockArts';
import styles from './Me.module.css';
import PFP from '../../assets/WA.jpg';
import editIcon from '../../assets/edit-profile-icon.svg'
import artsIcon from '../../assets/arts-icon.svg';
import ProfileOptionsMenu from '../../components/ProfileOptionsMenu/ProfileOptionsMenu';

import ArtCard from '../../components/ArtCard/ArtCard';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';

export default function Me() {
    const [isSubscribed, setIsSubscribed] = useState(false);

    const toggleSubscribe = () => {
        setIsSubscribed(!isSubscribed);
    };

    return (
        <>
            <div className={styles.headContent}>
                <div className={styles.faceName}>
                    <img src={PFP} alt="profile-photo" className={styles.pfp}/>
                    <span className={styles.nickname}>@
                        <span>some_name</span>
                    </span>
                </div>

                <div className={styles.contentWrapper}>
                    <div className={styles.headBg}></div>

                    <div className={styles.edit}>
                        <img src={editIcon} alt="arts" />
                        <span>Редактировать</span>
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
                            <span>{"some desription"}</span>
                        </div>

                        <div className={styles.buttonsCover}>
                            <ProfileOptionsMenu 
                                onPrivacyClick={() => alert('Настройки приватности')}
                                onDeleteClick={() => alert('Удаление профиля')}
                            />
                        </div> 
                    </div>
                </div>
            </div>


            <div className={styles.feed}>
                {mockArts.map(art => (
                    <ArtCard key={art.id} id={art.id} image={art.image} typeShow={"amount"} />
                ))}
            </div>
        </>
    );
}