public class SpiralMatrix {

	public static void main(String[] args) {
		int[][] matrix = {  {1,2,3,4,5,6},
							{7,8,9,10,11,12},						
							{13,14,15,16,17,18},
							{19,20,21,22,23,24},
							{25,26,27,28,29,30},
							{31,32,33,34,35,36}
						};
		
		int rows = matrix.length;
		int columns = matrix[0].length;
		
		int i = 0, j = rows;
		
		while(i < (j - 1)) {
			int row = i, column = i;
			//print horizontal
			if(row < rows) {
				for(;column < columns; column++) {
					System.out.print(matrix[row][column] + " ");
				}
			}
			
			column--;
			row++;			
			//print vertical
			for(;row < rows; row++) {
				System.out.print(matrix[row][column] + " ");
			}
			
			row--;
			column--;
			if(row > i) {
				//print reverse horizontal
				for(; column >= i; column--) {
					System.out.print(matrix[row][column] + " ");
				}		
			}
			
			column++;
			row--;
			//print reverse vertical
			for(; row > i; row--) {
				System.out.print(matrix[row][column] + " ");
			}			
			
			i++;
			rows--;
			columns--;
		}
	}
}
