import User from '../../../../src/core/user/User';
import { Provider } from '../../../../src/authentication/provider.enum';

describe('user', () => {
    it('should change the displayName of user', () => {
        const user = new User('user_id', 'user@email.com', 'display_name', 'avatar_url');

        expect(user.displayName).toBe('display_name');
        user.displayName = 'new_name';
        expect(user.displayName).toBe('new_name');
    });

    it('should change avatarUrl of user', () => {
        const user = new User('user_id', 'user@email.com', 'display_name', 'avatar_url');

        expect(user.avatarUrl).toBe('avatar_url');
        user.avatarUrl = 'new_avatar_url';
        expect(user.avatarUrl).toBe('new_avatar_url');
    });

    it('should change user has password', () => {
        const user = new User(
            'user_id',
            'user@email.com',
            'display_name',
            'avatar_url',
            Provider.GOOGLE,
            'FORCE_CHANGE_PASSWORD',
        );

        expect(user.hasPassword).toBe(false);
        user.status = 'random';
        expect(user.hasPassword).toBe(true);
    });
});