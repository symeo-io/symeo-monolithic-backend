export default class User {
    constructor(
        id,
        email,
        displayName,
        avatarUrl,
        status,
    ) {
        this.id = id;
        this.displayName = displayName || email;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.status = status;
    }

    get hasPassword() {
        return this.status !== 'FORCE_CHANGE_PASSWORD';
    }
}