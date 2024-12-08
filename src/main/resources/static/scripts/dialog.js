function dialogComponent() {
    return {
        visible: false,
        show() {
            console.log('Show dialog triggered'); // Debug log
            this.visible = true;
            this.$nextTick(() => {
                this.$el.querySelector('dialog').showModal();
            });
        },
        close() {
            console.log('Close dialog triggered'); // Debug log
            this.visible = false;
            this.$nextTick(() => {
                this.$el.querySelector('dialog').close();
            });
        }
    };
}