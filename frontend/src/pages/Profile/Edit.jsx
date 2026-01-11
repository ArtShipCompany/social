import { useState } from 'react';

import styles from './Edit.module.css';
import DefaultBtn from '../../components/DefaultBtn/DefaultBtn';
import PhotoIcon from '../../assets/edit-pfp.svg'
import blankPfp from '../../assets/blank-pfp.svg'

export default function Edit() {
    const MAX_LENGTH = 100;
    const [bio, setBio] = useState('');
    const [username, setUsername] = useState('');
    const [avatarUrl, setAvatarUrl] = useState(blankPfp);

    const handleAvatarChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onloadend = () => {
                setAvatarUrl(reader.result);
            };
            reader.readAsDataURL(file);
        }
    };

    return (
        <>
        <div className={styles.form}>

            <div className={styles.pfp}>
                <label htmlFor="avatarUpload" className={styles.avatarLabel}>
                    <img src={avatarUrl} alt="profile-photo" className={styles.avatarImg} />
                    <img src={PhotoIcon} alt="edit-photo" className={styles.photoIcon}/>
                </label>
                <input
                    id="avatarUpload"
                    type="file"
                    accept="image/*"
                    onChange={handleAvatarChange}
                    style={{ display: 'none' }}
                />
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