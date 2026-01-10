import { useState } from 'react';

import styles from './Edit.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import PhotoIcon from '../../assets/edit-pfp.svg'
import blankPfp from '../../assets/blank-pfp.svg'

export default function Edit() {
    const MAX_LENGTH = 100;
    const [bio, setBio] = useState('');
    const [username, setUsername] = useState('');

    return (
        <>
        <div className={styles.form}>

            <div className={styles.pfp}>
                <img src={blankPfp} alt="profile-photo" />
                <img src={PhotoIcon} alt="edit-photo" className={styles.photoIcon}/>
            </div>
            <span>Изменить фото</span>

            <div className={styles.inputGroup}>
                <div className={styles.nameInput}>
                    <label htmlFor="">Имя</label>
                    <div className={styles.usernameWrapper}>
                        <span className={styles.prefix}>@</span>
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => {
                                let value = e.target.value;

                                value = value.replace(/[^a-zA-Z0-9_]/g, '');

                                if (value.length <= MAX_LENGTH) {
                                    setUsername(value);
                                }
                            }}
                            placeholder="имя_пользователя"
                            className={styles.usernameInput}
                        />
                    </div>
                </div>
                
                <div className={styles.textareaWrapper}>
                    <label htmlFor="">Описание профиля</label>
                    <textarea
                        value={bio}
                        onChange={(e) => {
                            if (e.target.value.length <= MAX_LENGTH) {
                                setBio(e.target.value);
                            }
                        }}
                        maxLength={MAX_LENGTH}
                        placeholder="Пара слов.."
                        className={styles.bioTextarea}
                    />
                    <div className={styles.charCount}>
                        {bio.length}/{MAX_LENGTH}
                    </div>
                </div>
            </div>

            <DefaultBtn 
                text={'Сохранить'} 
                className={styles.loginBtn} 
            />

        </div>
        </>
    );
}