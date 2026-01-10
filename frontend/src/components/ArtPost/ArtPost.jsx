import { useState } from 'react';
import styles from './ArtPost.module.css';
import LikeBtn from '../LikeBtn/LikeBtn';
import DefaultBtn from '../DefaultBtn/DefaultBtn';

export default function ArtPost({edited = false}) {
    const MAX_LENGTH = 500;
    const [description, setDescription] = useState('');
    const [tags, setTags] = useState('');

    return(
        <div className={styles.artWrapp}>
            <div className={styles.artImage}></div>

            {!edited &&(
                <div className={styles.content}>
                    <div className={styles.textContent}>
                            <div className={styles.tags}>
                                <span>#kfldfk #dkslk #dfjsldf #dkfldskf</span>
                            </div>
                            <span>mamds podvvv;d|dspcdosk fdfldskf;dlfsd ;fkds;fdkf;fdlkfdfkdl fkld lf dkfldfk ldl kdfifk</span>
                        </div>
                        <LikeBtn className={styles.like}/>
                </div>
            )}
            
            {edited &&(
                <div className={styles.editContent}>
                    <div className={styles.form}>
                        <div className={styles.tags}>
                            <label htmlFor="tags">Тэги:</label>
                            <textarea
                                value={tags}
                                onChange={(e) => {
                                    if (e.target.value.length <= MAX_LENGTH) {
                                        setTags(e.target.value);
                                    }
                                }}
                                onInput={(e) => {
                                    e.target.style.height = 'auto';
                                    e.target.style.height = e.target.scrollHeight + 'px';
                                }}
                                maxLength={MAX_LENGTH}
                                placeholder="#rec #fyp #wenclair.."
                                className={styles.tagsTextarea}
                            />

                        </div>

                        <div className={styles.textareaWrapper}>
                            <label htmlFor="description">Описание:</label>
                            <textarea
                                value={description}
                                onChange={(e) => {
                                    if (e.target.value.length <= MAX_LENGTH) {
                                        setDescription(e.target.value);
                                    }
                                }}
                                onInput={(e) => {
                                    e.target.style.height = 'auto';
                                    e.target.style.height = e.target.scrollHeight + 'px';
                                }}
                                maxLength={MAX_LENGTH}
                                placeholder="Пара слов.."
                                className={styles.description}
                            />
                            <div className={styles.charCount}>
                                {description.length}/{MAX_LENGTH}
                            </div>
                        </div>
                    </div>
                    <div className={styles.btnArea}>
                        <DefaultBtn text={'Сохранить'}/>
                    </div>
                    
                </div>
            )}
        </div>
    );
}