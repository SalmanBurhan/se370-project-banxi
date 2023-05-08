(async ($) => {
    // Grab a Link token to initialize Link
    const createLinkToken = async () => {
        const res = await fetch("/banxi/link/create", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({})
        });
        const data = await res.json();
        const linkToken = data.link_token;
        localStorage.setItem("link_token", linkToken);
        // linkAccountButton.removeAttribute("disabled");
        return linkToken;
    };

    // Initialize Link
    const handler = Plaid.create({
        token: await createLinkToken(),
        onSuccess: async (publicToken, metadata) => {
            await fetch("/banxi/link/exchange", {
                method: "POST",
                body: JSON.stringify({
                    // client_id: client_id,
                    public_token: publicToken
                }),
                headers: {
                    "Content-Type": "application/json",
                },
            });
            //await getBalance();
        },
        onEvent: (eventName, metadata) => {
            console.log("Event:", eventName);
            console.log("Metadata:", metadata);
        },
        onExit: (error, metadata) => {
            console.log(error, metadata);
        },
    });

    // Start Link when button is clicked
    const linkAccountButton = document.getElementById("link-account");
    linkAccountButton.addEventListener("click", (event) => {
        handler.open();
    });
})(jQuery);
