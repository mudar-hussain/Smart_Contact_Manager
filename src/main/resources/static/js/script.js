console.log("This is script file");

const toggleSidebar=()=>{
	
	if($(".sidebar").is(":visible"))
	{
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
		$(".backBtn").css("display", "none");
		$(".barsBtn").css("display", "block");
		
	}else{
		
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "16%");
		$(".backBtn").css("display", "block");
		$(".barsBtn").css("display", "none");
	}
};

const search=()=>{
	// console.log("Searching...");
	
	let query = $("#search-input").val();
	
	if(query == ""){
		
		$(".search-result").hide();
	}else{
		// console.log(query);
		
		let url = `http://localhost:8090/user/search/${query}`;
		
		fetch(url)
			.then((response) => {
				return response.json();
			})
			.then((data) =>{
				console.log(data);
				
				let text = `<div class='list-group'>`;
				
				data.forEach((contact) => {
					text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-action'> ${contact.name} </a>`;
				});
				
				text += `</div>`;
				
				$(".search-result").html(text);
				$(".search-result").show();
			});
		
		
	}
	
}








