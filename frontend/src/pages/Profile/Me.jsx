import { useState } from 'react';
import { TEXTS } from '../../assets/texts';

import styles from './Me.module.css';
import PFP from '../../assets/WA.jpg';
import editIcon from '../../assets/edit-profile-icon.svg'
import artsIcon from '../../assets/arts-icon.svg';
import sortIcon from '../../assets/sort-arts-pf-icon.svg';

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
                            <span>{TEXTS.profileTest.desc}</span>
                        </div>

                        <div className={styles.buttonsCover}>
                            <button className={styles.sort}>
                                <img src={sortIcon} alt="sort" className={styles.icon} />
                            </button>
                        </div> 
                    </div>
                </div>
            </div>


            <div className={styles.feed}>
                {Array.from({ length: 8 }).map((_, i) => (
                    <ArtCard key={i} showLikeButton={false} />
                ))}
            </div>
        </>
    );
}