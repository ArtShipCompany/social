import { useState } from 'react';
import { Link } from 'react-router-dom';
import { mockArts } from '../../mock-images/mockArts';
import styles from './Me.module.css';
//mock
import PFP from '../../assets/WA.jpg';
import editIcon from '../../assets/edit-profile-icon.svg'
import artsIcon from '../../assets/arts-icon.svg';
import ProfileOptionsMenu from '../../components/ProfileOptionsMenu/ProfileOptionsMenu';
import ConfirmModal from '../../components/ConfirmModal/ConfirmModal';
import ArtCard from '../../components/ArtCard/ArtCard';

export default function Me() {
    // const [isSubscribed, setIsSubscribed] = useState(false);
    const [showDeleteIcons, setShowDeleteIcons] = useState(false);
    const [showPrivacyIcons, setShowPrivacyIcons] = useState(false);
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    // на будущее
    // const toggleSubscribe = () => {
    //     setIsSubscribed(!isSubscribed);
    // };
    const [showConfirmModal, setShowConfirmModal] = useState(false);
    const [modalArtId, setModalArtId] = useState(null);

    const closeMenuAndResetModes = () => {
        setShowDeleteIcons(false);
        setShowPrivacyIcons(false);
        setIsMenuOpen(false);
    };

    const toggleMenu = () => {
        if (isMenuOpen) {
            closeMenuAndResetModes();
        } else {
            setIsMenuOpen(true);
        }
    };

    const handlePrivacyClick = () => {
        setShowPrivacyIcons(prev => !prev);
        setShowDeleteIcons(false);
    };

    const handleDeleteClick = () => {
        setShowDeleteIcons(prev => !prev);
        setShowPrivacyIcons(false);
    };

    const openConfirmModal = (id) => {
        setModalArtId(id);
        setShowConfirmModal(true);
    };

    const confirmDelete = () => {
        alert(`Арт ${modalArtId} удалён!`);
        setShowConfirmModal(false);
        setModalArtId(null);
    };

    const cancelDelete = () => {
        setShowConfirmModal(false);
        setModalArtId(null);
    };

    return (
        <>
            <div className={styles.headContent}>
                <div className={styles.faceName}>
                    <img src={PFP} alt="profile-photo" className={styles.pfp}/>
                    <span className={styles.nickname}>@
                        {/* здесь никнем юзера */}
                        <span>some_name</span>
                    </span>
                </div>

                <div className={styles.contentWrapper}>
                    <div className={styles.headBg}></div>

                    {/* переход на Edit возможно нужно сделать передачу id? или нет*/}
                    {/* хотя если ты переходишь и так со своей страницы Me */}
                    <Link to="/edit" className={styles.edit}>
                        <img src={editIcon} alt="arts" />
                        <span>Редактировать</span>
                    </Link>

                    <div className={styles.headSFooter}>
                        <div className={styles.stats}>
                            <div className={styles.arts}>
                                <img src={artsIcon} alt="arts" />
                                {/* соответсвенно кол-во артов */}
                                <span>{' 111'}</span>
                            </div>
                            {/* кол-во */}
                            <span>Подписчики: {'5.5M'}</span>
                            <span>Подписки: {'505'}</span>
                        </div>

                        <div className={styles.bio}>
                            {/* так же передача сюда */}
                            <span>{"some desription"}</span>
                        </div>

                        <div className={styles.buttonsCover}>
                            <ProfileOptionsMenu 
                                isOpen={isMenuOpen}
                                onToggle={toggleMenu}
                                onPrivacyClick={handlePrivacyClick}
                                onDeleteClick={handleDeleteClick}
                            />
                        </div>  
                    </div>
                </div>
            </div>


            <div className={styles.feed}>
                {/* здесь то же не mockArts а арты пользователя */}
                {mockArts.map(art => (
                    <ArtCard 
                        key={art.id} 
                        id={art.id} 
                        image={art.image} 
                        typeShow={"amount"} 
                        showDeleteIcon={showDeleteIcons}
                        showPrivacyIcon={showPrivacyIcons}
                        onOpenConfirmModal={openConfirmModal}
                    />
                ))}
            </div>

            <ConfirmModal
                isOpen={showConfirmModal}
                onClose={cancelDelete}
                onConfirm={confirmDelete}
                title="Удаление арта"
                message={`Вы точно хотите удалить арт ${modalArtId}?`}
            />
        </>
    );
}