import styles from './SocialLinks.module.css';
import vkIcon from '../../assets/vk.svg';
import telegramIcon from '../../assets/telegram.svg';
import youtubeIcon from '../../assets/youtube.svg';
import twitterIcon from '../../assets/twitter.svg';
import tiktokIcon from '../../assets/tiktok.svg';

const PLATFORM_ICONS = {
    VKONTAKTE: vkIcon,
    TELEGRAM: telegramIcon,
    YOUTUBE: youtubeIcon,
    TWITTER: twitterIcon,
    TIKTOK: tiktokIcon,
};

export default function SocialLinks({ links = [] }) {
    if (!links || links.length === 0) return null;

    return (
        <div className={styles.socialLinks}>
            {links.map((link) => {
                const icon = PLATFORM_ICONS[link.platform];
                if (!icon || !link.visible) return null;

                return (
                    <a
                        key={link.id}
                        href={link.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className={styles.socialLink}
                        title={link.platform}
                    >
                        <img src={icon} alt={link.platform} className={styles.icon} />
                    </a>
                );
            })}
        </div>
    );
}