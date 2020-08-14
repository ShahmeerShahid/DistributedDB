const second = document.querySelector(".test");
const button = document.querySelector(".button");

const expandCard = () => {
    second.classList.add("hover");
};

button.addEventListener("click", expandCard);
