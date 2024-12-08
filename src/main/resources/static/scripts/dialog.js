function dialogComponent() {
    return {
        visible: false,
        show() {
            this.$el.showModal();
            this.$el.classList.add('dialog-visible');
        },
        close() {
            this.$el.close();
            this.$el.classList.remove('dialog-visible');
            this.$dispatch('dialog-closed');
        }
    };
}